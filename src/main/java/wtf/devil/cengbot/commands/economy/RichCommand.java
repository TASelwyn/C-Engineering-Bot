package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.MessageUtils;
import wtf.devil.cengbot.utils.database.model.BotUser;
import wtf.devil.cengbot.utils.database.repo.UserRepo;

import java.awt.*;

public class RichCommand {

    private static final UserRepo userRepo = new UserRepo();

    public RichCommand(MessageReceivedEvent event, String[] params) {

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.MAGENTA);

        StringBuilder stringBuilder = new StringBuilder();

        int i = 0;
        for (BotUser user : userRepo.findRichest(5)) {
            i++;
            switch (i) {
                case 1:
                    stringBuilder.append(":first_place:");
                    break;
                case 2:
                    stringBuilder.append(":second_place:");
                    break;
                case 3:
                    stringBuilder.append(":third_place:");
                    break;
                default:
                    stringBuilder.append(":small_blue_diamond:");
                    break;
            }
            stringBuilder.append(" **" + user.getCash() + "** - " + user.getNickname() + "\n");
        }
        embed.addField("Richest users "/*on " + event.getServer().get().getName()*/, stringBuilder.toString(), false);
        embed.setFooter("ROBBABLE CASH ONLY. Not net worth!");

        event.getChannel().sendMessageEmbeds(embed.build())
                .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
    }
}
