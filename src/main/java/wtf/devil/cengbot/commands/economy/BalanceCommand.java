package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.MessageUtils;
import wtf.devil.cengbot.utils.modules.Economy;

import java.awt.*;
import java.util.List;

import static wtf.devil.cengbot.Constants.numFormatter;
import static wtf.devil.cengbot.Constants.percentFormatter;

public class BalanceCommand {

    public BalanceCommand(MessageReceivedEvent event, String[] params) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.red);

        User user = event.getAuthor();

        // switch to mentioned user if there is one
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
        if (!mentionedUsers.isEmpty()) {
            if (!mentionedUsers.get(0).isBot()) {
                user = mentionedUsers.get(0);

            } else {
                embed.addField("This user is a bot.", "They have no balance.", false);
                event.getChannel().sendMessageEmbeds(embed.build())
                        .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
                return;
            }
        }

        Economy eco = new Economy();

        long cash = eco.getCash(user.getIdLong());
        long bank = eco.getBank(user.getIdLong());
        long netWorth = cash + bank;

        embed.addField("Cash $", numFormatter.format(cash), false);
        embed.addField("Bank $", (numFormatter.format(bank) + " (" + percentFormatter.format(eco.getVaultUsedPercentage(user.getIdLong())) + " full)"), false);
        embed.addField("Net Worth $", numFormatter.format(netWorth), false);
        embed.setAuthor(user.getName(), null, user.getEffectiveAvatarUrl());

        event.getChannel().sendMessageEmbeds(embed.build())
                .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
    }
}
