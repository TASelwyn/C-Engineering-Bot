package wtf.devil.cengbot.commands.dev;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import java.util.Locale;

import static wtf.devil.cengbot.Constants.numFormatter;

public class CheatCommand {
    public CheatCommand(MessageCreateEvent event, String[] params) {
        if (params.length >= 3) {
            if (event.getMessage().getMentionedUsers().size() > 0) {
                Economy eco = new Economy();
                Parsers stringParsers = new Parsers();

                long userID = event.getMessage().getMentionedUsers().get(0).getId();
                long dollars = stringParsers.parseStringToLong(params[2]);

                if (params[0].equalsIgnoreCase("add")) {
                    eco.addCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: ADDED $" + numFormatter.format(dollars) + " to " + event.getMessage().getMentionedUsers().get(0).getDisplayName(event.getServer().get()));
                } else if (params[0].equalsIgnoreCase("rem")) {
                    eco.addCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: REMOVED $" + numFormatter.format(dollars) + " from " + event.getMessage().getMentionedUsers().get(0).getDisplayName(event.getServer().get()));
                } else if (params[0].equalsIgnoreCase("cash")) {
                    eco.setCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: CASH SET TO $" + numFormatter.format(dollars) + " for " + event.getMessage().getMentionedUsers().get(0).getDisplayName(event.getServer().get()));
                } else if (params[0].equalsIgnoreCase("bank")) {
                    eco.setBank(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: BANK SET TO $" + numFormatter.format(dollars) + " for " + event.getMessage().getMentionedUsers().get(0).getDisplayName(event.getServer().get()));
                }
            }
        } else {
            event.getChannel().sendMessage("Invalid usage. `c.cheat cash <user> 5k`");
        }
    }
}
