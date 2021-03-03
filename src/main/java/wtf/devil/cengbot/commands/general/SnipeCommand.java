package wtf.devil.cengbot.commands.general;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;

public class SnipeCommand {
    public SnipeCommand(MessageCreateEvent event, String[] params) {
        //event.getChannel().sendMessage("EXAMPLE COMMAND");
        MessageAuthor author = event.getMessageAuthor();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("COMMAND NOT FUNCTIONAL YET --->")
                .addField("Old message", "a msg", false)
                .addField("New message", "not a command yet", false)
                //.addField("Edit timestamp", event.getMessage().get().getLastEditTimestamp().get().toString(), false)
                .setColor(Color.BLACK)
                .setAuthor(author);

        event.getChannel().sendMessage(embed)
                .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
    }
}
