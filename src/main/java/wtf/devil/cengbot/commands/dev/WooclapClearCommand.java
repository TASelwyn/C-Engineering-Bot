package wtf.devil.cengbot.commands.dev;

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
        MessageChannel logChannel = event.getJDA().getChannelById(MessageChannel.class, wooclapLogChannelId);
        if (logChannel == null) {
            event.getChannel().sendMessage("Couldn't find the Wooclap log channel.").queue();
            return;
        }

        logChannel.getIterableHistory().takeWhileAsync(message -> true).thenCompose(messages -> {
            WooclapLogRouter.startNewMessage();
            // purgeMessages bulk deletes recent messages and falls back to single deletes for ones older than 2 weeks
            return CompletableFuture.allOf(logChannel.purgeMessages(messages).toArray(CompletableFuture[]::new))
                    .thenApply(done -> messages.size());
        }).whenComplete((count, error) -> {
            if (error != null) {
                logger.warn("Failed to clear Wooclap log channel {}", wooclapLogChannelId, error);
                event.getChannel().sendMessage("Failed to clear the Wooclap log channel: " + error.getMessage()).queue();
                return;
            }

            // Log output after this starts a new message below the notice instead of editing one above it
            logChannel.sendMessage("Wooclapper channel cleared.").queue(sent -> WooclapLogRouter.startNewMessage());
            if (event.getChannel().getIdLong() != wooclapLogChannelId) {
                event.getChannel().sendMessage("Cleared " + count + " messages from <#" + wooclapLogChannelId + ">.").queue();
            }
        });
    }
}
