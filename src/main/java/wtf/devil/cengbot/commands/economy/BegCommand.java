package wtf.devil.cengbot.commands.economy;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.Economy;

import static wtf.devil.cengbot.Constants.numFormatter;

public class BegCommand {

    public BegCommand(MessageCreateEvent event, String[] params) {
        Economy eco = new Economy();

        long userID = event.getMessageAuthor().getId();

        long begAmount = (long) (Math.random() * (250 - 50 + 1) + 50);

        eco.addCash(userID, begAmount);

        event.getChannel().sendMessage("You fking beggar. Here's $" + numFormatter.format(begAmount) + "! You now have $" + numFormatter.format(eco.getCash(userID)));
    }
}