package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import static wtf.devil.cengbot.Constants.numFormatter;

public class DepositCommand {
    public DepositCommand(MessageReceivedEvent event, String[] params) {
        if (params.length == 1 && event.getMessage().getMentions().getUsers().isEmpty()) {
            Economy eco = new Economy();

            /// c.deposit max


            long callerDiscordID = event.getAuthor().getIdLong();

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
                event.getChannel().sendMessage("You deposited $" + numFormatter.format(valueToDeposit) + " into your bank's vault.").queue();
            } else if (maxDepositAmount == 0) {
                event.getChannel().sendMessage("Bro, your bank is full! Try upgrading your vault.").queue();
            } else if (valueToDeposit <= 0) {
                event.getChannel().sendMessage("Trying to deposit nothing, are we?").queue();
            } else {
                event.getChannel().sendMessage("You don't have enough money to perform that action.").queue();
            }

        } else {
            event.getChannel().sendMessage("Not quite sure how much you want me to deposit, so I did nothing. `c.help deposit`").queue();
        }
    }
}
