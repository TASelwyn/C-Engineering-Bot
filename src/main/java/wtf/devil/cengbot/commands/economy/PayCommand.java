package wtf.devil.cengbot.commands.economy;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import java.util.Optional;

import static wtf.devil.cengbot.Constants.numFormatter;

public class PayCommand {
    public PayCommand(MessageReceivedEvent event, String[] params) {
        if (params.length == 2) {

            long callerDiscordID = event.getAuthor().getIdLong();
            long mentionedUserID = event.getMessage().getMentions().getUsers().get(0).getIdLong();

            if (!event.getMessage().getMentions().getUsers().isEmpty() && callerDiscordID != mentionedUserID) {
                Parsers stringParsers = new Parsers();

                long value = stringParsers.parseStringToLong(params[1]);

                Economy eco = new Economy();


                // VERY BROKEN LOGIC I NEED TO FIX
                // NO HATE PLZ I DID THIS STUPID QUICK AND ITS BROKEN NOW CUZ OF Economy CHANGES :(
                // TODO
                if (eco.getCash(callerDiscordID) >= value) {
                    eco.payCash(callerDiscordID, mentionedUserID, value);
                    if (event.isFromGuild()) {
                        event.getChannel().sendMessage("You gave $" + numFormatter.format(value) + " to " + event.getMessage().getMentions().getUsers().get(0).getName() + "!").queue();
                    }

                } else {
                    event.getChannel().sendMessage("You do not have enough money to give them.").queue();
                }
            } else if (callerDiscordID != mentionedUserID) {

                if (event.isFromGuild()) {
                    Optional<RichCustomEmoji> sadKekEmoji = Optional.ofNullable(event.getGuild().getEmojiById(755840964077289532L));
                    sadKekEmoji.ifPresent(customEmoji -> {
                        event.getChannel().sendMessage("You can't pay yourself! " + customEmoji.getFormatted()).queue();
                    });
                }

            } else {
                event.getChannel().sendMessage("Invalid usage. `c.help pay`").queue();
            }
        } else {
            event.getChannel().sendMessage("Invalid usage. `c.help pay`").queue();
        }
    }
}
