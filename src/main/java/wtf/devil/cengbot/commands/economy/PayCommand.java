package wtf.devil.cengbot.commands.economy;

import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.modules.Parsers;

import java.util.Optional;

import static wtf.devil.cengbot.Constants.numFormatter;

public class PayCommand {
    public PayCommand(MessageCreateEvent event, String[] params) {
        if (params.length == 2) {

            long callerDiscordID = event.getMessageAuthor().getId();
            long mentionedUserID = event.getMessage().getMentionedUsers().get(0).getId();

            if (event.getMessage().getMentionedUsers().size() > 0 && callerDiscordID != mentionedUserID) {
                Parsers stringParsers = new Parsers();

                long value = stringParsers.parseStringToLong(params[1]);

                Economy eco = new Economy();


                // VERY BROKEN LOGIC I NEED TO FIX
                // NO HATE PLZ I DID THIS STUPID QUICK AND ITS BROKEN NOW CUZ OF Economy CHANGES :(
                // TODO
                if (eco.getCash(callerDiscordID) >= value) {
                    eco.payCash(callerDiscordID, mentionedUserID, value);
                    event.getServer().ifPresent(activeServer -> {
                        event.getChannel().sendMessage("You gave $" + numFormatter.format(value) + " to " + event.getMessage().getMentionedUsers().get(0).getDisplayName(activeServer) + "!");
                    });

                } else {
                    event.getChannel().sendMessage("You do not have enough money to give them.");
                }
            } else if (callerDiscordID != mentionedUserID) {

                Optional<CustomEmoji> sadKekEmoji = event.getApi().getCustomEmojiById(755840964077289532L).get().asCustomEmoji();
                sadKekEmoji.ifPresent(customEmoji -> {
                    event.getChannel().sendMessage("You can't pay yourself! " + customEmoji.getMentionTag());
                });

            } else {
                event.getChannel().sendMessage("Invalid usage. `c.help pay`");
            }
        } else {
            event.getChannel().sendMessage("Invalid usage. `c.help pay`");
        }
    }
}