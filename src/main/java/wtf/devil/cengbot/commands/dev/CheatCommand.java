package wtf.devil.cengbot.commands.dev;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import java.util.List;

import static wtf.devil.cengbot.Constants.numFormatter;

public class CheatCommand {
    public CheatCommand(MessageReceivedEvent event, String[] params) {
        if (params.length >= 3) {
            List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
            if (!mentionedUsers.isEmpty()) {
                Economy eco = new Economy();
                Parsers stringParsers = new Parsers();

                User mentionedUser = mentionedUsers.get(0);
                long userID = mentionedUser.getIdLong();
                long dollars = stringParsers.parseStringToLong(params[2]);

                List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
                String displayName = !mentionedMembers.isEmpty() ? mentionedMembers.get(0).getEffectiveName() : mentionedUser.getName();

                if (params[0].equalsIgnoreCase("add")) {
                    eco.addCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: ADDED $" + numFormatter.format(dollars) + " to " + displayName).queue();
                } else if (params[0].equalsIgnoreCase("rem")) {
                    eco.addCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: REMOVED $" + numFormatter.format(dollars) + " from " + displayName).queue();
                } else if (params[0].equalsIgnoreCase("cash")) {
                    eco.setCash(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: CASH SET TO $" + numFormatter.format(dollars) + " for " + displayName).queue();
                } else if (params[0].equalsIgnoreCase("bank")) {
                    eco.setBank(userID, dollars);
                    event.getChannel().sendMessage("CHEAT: BANK SET TO $" + numFormatter.format(dollars) + " for " + displayName).queue();
                }
            }
        } else {
            event.getChannel().sendMessage("Invalid usage. `c.cheat cash <user> 5k`").queue();
        }
    }
}
