package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class WooclapCommand {
    public WooclapCommand(MessageReceivedEvent event, String[] params) {
        if (params.length == 2) {

            long callerDiscordID = event.getAuthor().getIdLong();
            //long mentionedUserID = event.getMessage().getMentions().getUsers().get(0).getIdLong();

            String eventCode = params[0].toUpperCase();
            String authToken = params[1];


        } else {
            event.getChannel().sendMessage("Invalid usage. `c.help wooclap`").queue();
        }
    }
}
