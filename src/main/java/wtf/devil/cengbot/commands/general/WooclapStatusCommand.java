package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import wtf.devil.cengbot.utils.modules.WooclapParticipants;
import wtf.devil.cengbot.utils.modules.WooclapParticipants.Member;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/*
 * Lists every Wooclap event the bot is answering, who's in each (grouped by priority, in answer order),
 * and who's waiting to enter a token for one.
 */
public class WooclapStatusCommand {

    public static final String SLASH_NAME = "status";

    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Post every Wooclap event the bot is in and who's in each to this channel.");

    private static final int DISCORD_LIMIT = 2000;

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        Map<String, List<Member>> joined = WooclapParticipants.participantsByEvent();
        Map<String, List<Long>> pending = WooclapCommand.pendingByEvent();

        TreeSet<String> eventCodes = new TreeSet<>(joined.keySet());
        eventCodes.addAll(pending.keySet());

        if (eventCodes.isEmpty()) {
            event.reply("The bot isn't in any Wooclap events right now.").queue();
            return;
        }

        StringBuilder reply = new StringBuilder("**Wooclap status**");
        for (String eventCode : eventCodes) {
            List<Member> in = joined.getOrDefault(eventCode, List.of());
            List<Long> waiting = pending.getOrDefault(eventCode, List.of());

            reply.append("\n\n`").append(eventCode).append("` (").append(in.size()).append(" in)");
            if (!in.isEmpty()) {
                reply.append("\nIn, by priority (lower answers first):");
                byPriority(in).forEach((priority, discordIDs) ->
                        reply.append("\n`").append(priority).append("` ").append(mentions(discordIDs)));
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

    // Lowest priority first, matching the order Wooclapper answers in
    private static Map<Integer, List<Long>> byPriority(List<Member> members) {
        return members.stream().collect(Collectors.groupingBy(Member::priority, TreeMap::new,
                Collectors.mapping(Member::discordID, Collectors.toList())));
    }

    private static String mentions(List<Long> discordIDs) {
        return discordIDs.stream().sorted().map(id -> "<@" + id + ">").collect(Collectors.joining(" "));
    }
}
