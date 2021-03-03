package wtf.devil.cengbot.commands;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.event.message.MessageCreateEvent;

public class ExampleCommand {

    public ExampleCommand(MessageCreateEvent event, String[] params) {

        MessageAuthor author = event.getMessage().getAuthor();

    }
}
