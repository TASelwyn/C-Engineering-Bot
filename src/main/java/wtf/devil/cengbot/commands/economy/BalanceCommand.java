package wtf.devil.cengbot.commands.economy;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;
import wtf.devil.cengbot.utils.modules.Economy;

import java.awt.*;

import static wtf.devil.cengbot.Constants.numFormatter;
import static wtf.devil.cengbot.Constants.percentFormatter;

public class BalanceCommand {

    public BalanceCommand(MessageCreateEvent event, String[] params) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.red);

        User user = event.getMessage().getAuthor().asUser().get();

        // switch to mentioned user if there is one
        if (event.getMessage().getMentionedUsers().size() > 0) {
            if (!event.getMessage().getMentionedUsers().get(0).isBot()) {
                user = event.getMessage().getMentionedUsers().get(0);

            } else {
                embed.addField("This user is a bot.", "They have no balance.", false);
                event.getChannel().sendMessage(embed)
                        .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
                return;
            }
        }

        Economy eco = new Economy();

        long cash = eco.getCash(user.getId());
        long bank = eco.getBank(user.getId());
        long netWorth = cash + bank;

        embed.addField("Cash $", numFormatter.format(cash), false);
        embed.addField("Bank $", (numFormatter.format(bank) + " (" + percentFormatter.format(eco.getVaultUsedPercentage(user.getId())) + " full)"), false);
        embed.addField("Net Worth $", numFormatter.format(netWorth), false);
        embed.setAuthor(user);

        event.getChannel().sendMessage(embed)
                .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
    }
}