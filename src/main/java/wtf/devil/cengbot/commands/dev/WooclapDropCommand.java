package wtf.devil.cengbot.commands.dev;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.commands.general.WooclapCommand;

import java.util.List;
import java.util.Set;

/*
 * Drops other users out of a Wooclap event, so the bot stops answering it for them.
 *   c.wooclapdrop <code> @user [@user ...]   (raw user IDs work too)
 */
public class WooclapDropCommand {

    public WooclapDropCommand(MessageReceivedEvent event, String[] params) {
        if (params.length < 2) {
            event.getChannel().sendMessage("Invalid usage. `c.wooclapdrop <event_code> @user [@user ...]`").queue();
            return;
        }

        String eventCode = params[0].toUpperCase();

        Set<Long> targets = WooclapPullCommand.parseTargets(event, params);
        if (targets.isEmpty()) {
            event.getChannel().sendMessage("Mention at least one user (or give their user ID) to drop.").queue();
            return;
        }

        StringBuilder report = new StringBuilder("Wooclap `" + eventCode + "` drop results:");
        for (long discordID : targets) {
            String result = switch (WooclapCommand.drop(discordID, eventCode)) {
                case DROPPED -> "Dropped.";
                case CANCELLED -> "Cancelled their queued join.";
                case NOT_IN -> "Wasn't in this event.";
            };
            report.append("\n<@").append(discordID).append(">: ").append(result);
        }

        event.getChannel().sendMessage(report.toString())
                // Lists who was dropped without pinging all of them
                .setAllowedMentions(List.of())
                .queue();
    }
}
