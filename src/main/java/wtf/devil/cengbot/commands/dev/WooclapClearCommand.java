package wtf.devil.cengbot.commands.dev;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.utils.modules.WooclapLogRouter;

import java.util.concurrent.CompletableFuture;

import static wtf.devil.cengbot.Constants.wooclapLogChannelId;

// Deletes every message in the Wooclap log channel
public class WooclapClearCommand {

    private static final Logger logger = LogManager.getLogger(WooclapClearCommand.class);

    public WooclapClearCommand(MessageReceivedEvent event, String[] params) {
        if (event.getJDA().getChannelById(MessageChannel.class, wooclapLogChannelId) == null) {
            event.getChannel().sendMessage("Couldn't find the Wooclap log channel.").queue();
            return;
        }

        clearLogChannel(event.getJDA(), "Wooclapper channel cleared.").whenComplete((count, error) -> {
            if (error != null) {
                event.getChannel().sendMessage("Failed to clear the Wooclap log channel: " + error.getMessage()).queue();
            } else if (event.getChannel().getIdLong() != wooclapLogChannelId) {
                event.getChannel().sendMessage("Cleared " + count + " messages from <#" + wooclapLogChannelId + ">.").queue();
            }
        });
    }

    /**
     * Deletes every message in the Wooclap log channel, then posts the notice.
     * Completes with the number of messages deleted.
     */
    public static CompletableFuture<Integer> clearLogChannel(JDA jda, String notice) {
        MessageChannel logChannel = jda.getChannelById(MessageChannel.class, wooclapLogChannelId);
        if (logChannel == null) {
            logger.warn("Wooclap log channel {} is unavailable, not clearing it", wooclapLogChannelId);
            return CompletableFuture.failedFuture(new IllegalStateException("Wooclap log channel not found"));
        }

        return logChannel.getIterableHistory().takeWhileAsync(message -> true).thenCompose(messages -> {
            WooclapLogRouter.startNewMessage();
            // purgeMessages bulk deletes recent messages and falls back to single deletes for ones older than 2 weeks
            return CompletableFuture.allOf(logChannel.purgeMessages(messages).toArray(CompletableFuture[]::new))
                    .thenApply(done -> messages.size());
        }).whenComplete((count, error) -> {
            if (error != null) {
                logger.warn("Failed to clear Wooclap log channel {}", wooclapLogChannelId, error);
                return;
            }
            // Log output after this starts a new message below the notice instead of editing one above it
            logChannel.sendMessage(notice).queue(sent -> WooclapLogRouter.startNewMessage());
        });
    }
}
