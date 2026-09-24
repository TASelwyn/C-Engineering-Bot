package wtf.devil.cengbot.commands.general;

import org.javacord.api.event.message.MessageCreateEvent;

public class WooclapCommand {
    public WooclapCommand(MessageCreateEvent event, String[] params) {
        if (params.length == 2) {

            long callerDiscordID = event.getMessageAuthor().getId();
            //long mentionedUserID = event.getMessage().getMentionedUsers().get(0).getId();

            String eventCode = params[0].toUpperCase();
            String authToken = params[1];


        } else {
            event.getChannel().sendMessage("Invalid usage. `c.help wooclap`");
        }
    }
}