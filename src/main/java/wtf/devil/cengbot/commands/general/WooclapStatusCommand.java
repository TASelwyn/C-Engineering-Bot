package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import wtf.devil.cengbot.utils.watchers.WooclapRevokeWatcher;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/*
 * Lists every Wooclap event the bot is answering, who's in each, and who's waiting to enter a token for one.
 */
public class WooclapStatusCommand {

    public static final String SLASH_NAME = "status";

    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Post every Wooclap event the bot is in and who's in each to this channel.");

    private static final int DISCORD_LIMIT = 2000;

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        Map<String, List<Long>> joined = WooclapRevokeWatcher.participantsByEvent();
        Map<String, List<Long>> pending = WooclapCommand.pendingByEvent();

        TreeSet<String> eventCodes = new TreeSet<>(joined.keySet());
        eventCodes.addAll(pending.keySet());

        if (eventCodes.isEmpty()) {
            event.reply("The bot isn't in any Wooclap events right now.").queue();
            return;
        }

        StringBuilder reply = new StringBuilder("**Wooclap status**");
        for (String eventCode : eventCodes) {
            List<Long> in = joined.getOrDefault(eventCode, List.of());
            List<Long> waiting = pending.getOrDefault(eventCode, List.of());

            reply.append("\n\n`").append(eventCode).append("` (").append(in.size()).append(" in)");
            if (!in.isEmpty()) {
                reply.append("\nIn: ").append(mentions(in));
            }
            if (!waiting.isEmpty()) {
                reply.append("\nWaiting for a token: ").append(mentions(waiting));
            }
        }

        String text = reply.toString();
        if (text.length() > DISCORD_LIMIT) {
            text = text.substring(0, DISCORD_LIMIT - 20) + "\n... (truncated)";
        }

        event.reply(text)
                // Lists users without pinging them
                .setAllowedMentions(List.of())
                .queue();
    }

    private static String mentions(List<Long> discordIDs) {
        return discordIDs.stream().sorted().map(id -> "<@" + id + ">").collect(Collectors.joining(" "));
    }
}
