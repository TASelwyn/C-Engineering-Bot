package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.Economy;

import java.util.List;
import java.util.Optional;

import static wtf.devil.cengbot.Constants.numFormatter;

public class RobCommand {

    public RobCommand(MessageReceivedEvent event, String[] params) {
        List<User> userList = event.getMessage().getMentions().getUsers();

        main:
        if (!userList.isEmpty() && !userList.get(0).isBot() && event.getAuthor().getIdLong() != userList.get(0).getIdLong()) {

            Economy eco = new Economy();

            long callerDiscordID = event.getAuthor().getIdLong();
            long robbedDiscordID = userList.get(0).getIdLong();
            long cashAvailableToRob = eco.getCash(robbedDiscordID);

            if (cashAvailableToRob == 0) {
                event.getChannel().sendMessage("They are kinda broke man, don't steal from the poor. Try `c.rich`").queue();
                break main;
            }

            long robbedAmount = (long) (Math.random() * ((cashAvailableToRob / 5) - 50 + 1) + 50);

            eco.robUser(callerDiscordID, robbedDiscordID, robbedAmount);

            if (cashAvailableToRob == robbedAmount) {
                event.getChannel().sendMessage("Woahh! You robbed him for all he's worth! :money_mouth: You gained $" + numFormatter.format(robbedAmount)).queue();
                break main;
            } else {
                event.getChannel().sendMessage("You robbed him good!! :money_mouth: You gained $" + numFormatter.format(robbedAmount)).queue();
            }
        } else if (userList.isEmpty()) {
            event.getChannel().sendMessage("You need to mention someone in order to rob them!").queue();
        } else if (userList.get(0).isBot()){
            event.getChannel().sendMessage("Broo, bots don't have money for you to rob. Good try tho :smirk:").queue();
        } else if (userList.get(0).getIdLong() == event.getAuthor().getIdLong()) {

            if (event.isFromGuild()) {
                Optional<RichCustomEmoji> sadKekEmoji = Optional.ofNullable(event.getGuild().getEmojiById(755840964077289532L));
                sadKekEmoji.ifPresent(customEmoji -> {
                    event.getChannel().sendMessage("You can't rob yourself! " + customEmoji.getFormatted()).queue();
                });
            }
        }
    }
}
