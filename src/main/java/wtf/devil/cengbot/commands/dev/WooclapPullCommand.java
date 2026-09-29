package wtf.devil.cengbot.commands.dev;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import wtf.devil.cengbot.commands.CommandManager;
import wtf.devil.cengbot.commands.general.WooclapCommand;
import wtf.devil.cengbot.commands.general.WooclapCommand.PullResult;
import wtf.devil.cengbot.utils.modules.Parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/*
 * Joins other users into a Wooclap event using their saved tokens.
 *   c.wooclappull <code> @user [@user ...]   (raw user IDs work too)
 *   /pull <code> <users>
 */
public class WooclapPullCommand {

    public static final String SLASH_NAME = "pull";

    // Hidden from non-admins in the command list; onSlashCommand checks again since server settings can override that
    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Join someone into a Wooclap event with their saved token (DEV only).")
            .addOption(OptionType.STRING, "code", "The Wooclap event code", true)
            .addOption(OptionType.STRING, "users", "Who to pull in, as @mentions or user IDs", true)
            .setContexts(InteractionContextType.GUILD)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

    public WooclapPullCommand(MessageReceivedEvent event, String[] params) {
        if (params.length < 2) {
            event.getChannel().sendMessage("Invalid usage. `c.wooclappull <event_code> @user [@user ...]`").queue();
            return;
        }

        String eventCode = params[0].toUpperCase();

        Set<Long> targets = parseTargets(event, params);
        if (targets.isEmpty()) {
            event.getChannel().sendMessage("Mention at least one user (or give their user ID) to pull in.").queue();
            return;
        }

        event.getChannel().sendMessage("Pulling " + targets.size() + " user(s) into Wooclap `" + eventCode + "`...").queue();

        CompletableFuture.supplyAsync(() -> pull(eventCode, targets, event.getChannel()))
                .thenAccept(report -> event.getChannel().sendMessage(report)
                        // Lists who was pulled without pinging all of them
                        .setAllowedMentions(List.of())
                        .queue());
    }

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        if (!CommandManager.isDev(event.getMember())) {
            event.reply("No permissions.").setEphemeral(true).queue();
            return;
        }

        OptionMapping codeOption = event.getOption("code");
        OptionMapping usersOption = event.getOption("users");
        if (codeOption == null || usersOption == null) {
            event.reply("Invalid usage. `/pull <code> <users>`").setEphemeral(true).queue();
            return;
        }

        Set<Long> targets = Parsers.parseUserIds(usersOption.getAsString(), usersOption.getMentions());
        if (targets.isEmpty()) {
            event.reply("I couldn't find anyone in `users`. @mention them or paste their user IDs.").setEphemeral(true).queue();
            return;
        }

        String eventCode = codeOption.getAsString().trim().toUpperCase();
        MessageChannel channel = event.getChannel();

        // Joining hits the network, so defer to avoid the 3 second interaction timeout
        event.deferReply().queue(hook ->
                CompletableFuture.supplyAsync(() -> pull(eventCode, targets, channel))
                        .thenAccept(report -> hook.editOriginal(report)
                                .setAllowedMentions(List.of())
                                .queue()));
    }

    /*
     * Pulls each user into the event and returns a report line per user. Blocks, so call it off the JDA threads.
     * Users with no saved token are pinged in the given channel, and users whose token was rejected in the log channel.
     */
    static String pull(String eventCode, Collection<Long> targets, MessageChannel channel) {
        StringBuilder report = new StringBuilder("Wooclap `" + eventCode + "` pull results:");
        List<Long> needToken = new ArrayList<>();
        List<Long> tokenRejected = new ArrayList<>();
        for (long discordID : targets) {
            PullResult result = WooclapCommand.pullWithSavedToken(discordID, eventCode);
            report.append("\n<@").append(discordID).append(">: ").append(result.message());
            switch (result.status()) {
                case NO_TOKEN -> needToken.add(discordID);
                case TOKEN_REJECTED -> tokenRejected.add(discordID);
                case JOINED, FAILED -> {}
            }
        }

        if (!needToken.isEmpty()) {
            // Whoever clicks enters their own token, and their queued event is joined once it's saved
            String mentions = needToken.stream().map(id -> "<@" + id + ">").collect(Collectors.joining(" "));
            channel.sendMessage(mentions + " you're being pulled into Wooclap `" + eventCode
                            + "`, but I don't have your auth token saved. Click Enter token (or use `/settoken`) and I'll join you right away.")
                    .setComponents(WooclapCommand.tokenButtonRow())
                    .queue();
        }

        // Failures that aren't token problems (e.g. Wooclap being down) aren't pinged, since a new token wouldn't help
        if (!tokenRejected.isEmpty()) {
            WooclapCommand.pingForToken(tokenRejected, "you're being pulled into Wooclap `" + eventCode
                    + "`, but Wooclap rejected your saved auth token. Enter a new one below (or with `/settoken`) and I'll join you right away.");
        }
        return report.toString();
    }

    // Users named after the event code, by mention or raw user ID. Bots are ignored.
    static Set<Long> parseTargets(MessageReceivedEvent event, String[] params) {
        String names = String.join(" ", Arrays.copyOfRange(params, 1, params.length));
        return Parsers.parseUserIds(names, event.getMessage().getMentions());
    }
}
