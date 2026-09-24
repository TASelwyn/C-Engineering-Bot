package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.Economy;

import static wtf.devil.cengbot.Constants.numFormatter;

public class BegCommand {

    public BegCommand(MessageReceivedEvent event, String[] params) {
        Economy eco = new Economy();

        long userID = event.getAuthor().getIdLong();

        long begAmount = (long) (Math.random() * (250 - 50 + 1) + 50);

        eco.addCash(userID, begAmount);

        event.getChannel().sendMessage("You fking beggar. Here's $" + numFormatter.format(begAmount) + "! You now have $" + numFormatter.format(eco.getCash(userID))).queue();
    }
}
