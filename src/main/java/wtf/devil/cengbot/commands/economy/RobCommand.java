package wtf.devil.cengbot.commands.economy;

import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.utils.modules.Economy;
import java.util.List;
import java.util.Optional;

import static wtf.devil.cengbot.Constants.numFormatter;

public class RobCommand {

    public RobCommand(MessageCreateEvent event, String[] params) {
        List userList = event.getMessage().getMentionedUsers();

        main:
        if (userList.size() > 0 && !event.getMessage().getMentionedUsers().get(0).isBot() && event.getMessageAuthor().getId() != event.getMessage().getMentionedUsers().get(0).getId()) {

            Economy eco = new Economy();

            long callerDiscordID = event.getMessageAuthor().getId();
            long robbedDiscordID = event.getMessage().getMentionedUsers().get(0).getId();
            long cashAvailableToRob = eco.getCash(robbedDiscordID);

            if (cashAvailableToRob == 0) {
                event.getChannel().sendMessage("They are kinda broke man, don't steal from the poor. Try `c.rich`");
                break main;
            }

            long robbedAmount = (long) (Math.random() * ((cashAvailableToRob / 5) - 50 + 1) + 50);

            eco.robUser(callerDiscordID, robbedDiscordID, robbedAmount);

            if (cashAvailableToRob == robbedAmount) {
                event.getChannel().sendMessage("Woahh! You robbed him for all he's worth! :money_mouth: You gained $" + numFormatter.format(robbedAmount));
                break main;
            } else {
                event.getChannel().sendMessage("You robbed him good!! :money_mouth: You gained $" + numFormatter.format(robbedAmount));
            }
        } else if (userList.size() == 0) {
            event.getChannel().sendMessage("You need to mention someone in order to rob them!");
        } else if (event.getMessage().getMentionedUsers().get(0).isBot()){
            event.getChannel().sendMessage("Broo, bots don't have money for you to rob. Good try tho :smirk:");
        } else if (event.getMessage().getMentionedUsers().get(0).getId() == event.getMessageAuthor().getId()) {

            event.getApi().getCustomEmojiById(755840964077289532L).get().asCustomEmoji().ifPresent(customEmoji -> {
                event.getChannel().sendMessage("You can't rob yourself! " + customEmoji.getMentionTag());
            });
        }
    }
}