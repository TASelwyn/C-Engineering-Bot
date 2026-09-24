package wtf.devil.cengbot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ExampleCommand {

    public ExampleCommand(MessageReceivedEvent event, String[] params) {

        User author = event.getAuthor();

    }
}
