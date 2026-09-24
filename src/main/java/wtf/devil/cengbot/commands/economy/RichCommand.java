package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.MessageUtils;
import wtf.devil.cengbot.utils.database.UserDatabase;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.sql.SQLException;

public class RichCommand {

    public RichCommand(MessageReceivedEvent event, String[] params) {

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.MAGENTA);

        try {
            CachedRowSet crs = UserDatabase.getRichest(5);
            StringBuilder stringBuilder = new StringBuilder();


            int i = 0;
            while (crs.next()) {
                //value = crs.getString(1);
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
                stringBuilder.append(" **" + crs.getString(1) + "** - " + crs.getString(3) + "\n") ;
                //System.out.println("VALUEeEE: " + crs.getString(1) + " - " + crs.getString(2) + " - " + crs.getString(3) + " - " + crs.getString(4));
            }
            embed.addField("Richest users "/*on " + event.getServer().get().getName()*/, stringBuilder.toString(), false);
            embed.setFooter("ROBBABLE CASH ONLY. Not net worth!");

            event.getChannel().sendMessageEmbeds(embed.build())
                    .queue(null, MessageUtils.IGNORE_MISSING_PERMS);

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
