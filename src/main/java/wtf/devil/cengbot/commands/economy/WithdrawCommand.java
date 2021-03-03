package wtf.devil.cengbot.commands.economy;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import static wtf.devil.cengbot.Constants.numFormatter;

public class WithdrawCommand {
    public WithdrawCommand(MessageCreateEvent event, String[] params) {
        if (params.length == 1 && event.getMessage().getMentionedUsers().size() == 0 && params[0].matches("[A-Za-z0-9]+")) {
            Economy eco = new Economy();

            long callerDiscordID = event.getMessageAuthor().getId();

            long valueToWithdraw;
            long maxWithdrawalAmount = eco.getBank(callerDiscordID);

            if (params[0].equalsIgnoreCase("max") || params[0].equalsIgnoreCase("all")) {
                valueToWithdraw = maxWithdrawalAmount;
            } else {
                valueToWithdraw = new Parsers().parseStringToLong(params[0]);
            }

            System.out.println("BITCH: " + valueToWithdraw + " - " + maxWithdrawalAmount);


            if (valueToWithdraw > 0 && maxWithdrawalAmount > valueToWithdraw) {
                eco.withdrawBalance(callerDiscordID, valueToWithdraw);
                event.getChannel().sendMessage("You withdrew $" + numFormatter.format(valueToWithdraw));
            } else if (valueToWithdraw == maxWithdrawalAmount) {
                eco.withdrawMax(callerDiscordID);
                event.getChannel().sendMessage("You withdrew everything! Be careful bro");
            } else if (valueToWithdraw <= 0) {
                event.getChannel().sendMessage("You cannot withdraw nothing. Nice try though :smirk:");
            } else {
                event.getChannel().sendMessage("You don't have enough in your bank to do that!");
            }

        } else {
            event.getChannel().sendMessage("Try again, but this time tell me how much you wanna withdraw! `c.help withdraw`");
        }
    }
}