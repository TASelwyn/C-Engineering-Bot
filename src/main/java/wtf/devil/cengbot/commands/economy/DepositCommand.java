package wtf.devil.cengbot.commands.economy;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import static wtf.devil.cengbot.Constants.numFormatter;

public class DepositCommand {
    public DepositCommand(MessageCreateEvent event, String[] params) {
        if (params.length == 1 && event.getMessage().getMentionedUsers().size() == 0) {
            Economy eco = new Economy();

            /// c.deposit max


            long callerDiscordID = event.getMessageAuthor().getId();

            long valueToDeposit;
            long maxDepositAmount = eco.getMaxDepositAmount(callerDiscordID);
            long cashInHand = eco.getCash(callerDiscordID);


            //eco.depositBalance(callerDiscordID, params[0]);
            if (params[0].equalsIgnoreCase("max") || params[0].equalsIgnoreCase("all")) {
                if (maxDepositAmount > cashInHand) {
                    valueToDeposit = cashInHand;
                } else {
                    valueToDeposit = maxDepositAmount;
                }
            } else {
                valueToDeposit = new Parsers().parseStringToLong(params[0]);
            }


            if (valueToDeposit > 0 && (valueToDeposit <= cashInHand)) {
                eco.depositBalance(callerDiscordID, valueToDeposit);
                event.getChannel().sendMessage("You deposited $" + numFormatter.format(valueToDeposit) + " into your bank's vault.");
            } else if (maxDepositAmount == 0) {
                event.getChannel().sendMessage("Bro, your bank is full! Try upgrading your vault.");
            } else if (valueToDeposit <= 0) {
                event.getChannel().sendMessage("Trying to deposit nothing, are we?");
            } else {
                event.getChannel().sendMessage("You don't have enough money to perform that action.");
            }

        } else {
            event.getChannel().sendMessage("Not quite sure how much you want me to deposit, so I did nothing. `c.help deposit`");
        }
    }
}
