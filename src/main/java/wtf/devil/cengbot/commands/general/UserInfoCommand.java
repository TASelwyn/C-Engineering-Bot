package wtf.devil.cengbot.commands.general;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class UserInfoCommand {
    public UserInfoCommand(MessageCreateEvent event, String[] params) {
        //MessageAuthor author = event.getMessage().getAuthor();

        Server activeServer = event.getServer().get();
        User user = event.getMessage().getAuthor().asUser().get();

        // Grabs a mention if there is one, if not it'll just default to the message author.
        if (event.getMessage().getMentionedUsers().size() > 0) {
            user = event.getMessage().getMentionedUsers().get(0);
        }


        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("User Info")
                .addField("Display Name", user.getDisplayName(activeServer), true)
                .addField("Name + Discriminator", user.getDiscriminatedName(), true)
                .addField("User Id", user.getIdAsString(), true)
                .setAuthor(user);
        // Keep in mind that a message author can either be a webhook or a normal user
        //author.asUser().ifPresent(user -> {
        //embed.addField("Online Status", user.getStatus().getStatusString(), true);
        //embed.addField("Connected Clients", user.getCurrentClients().toString());
        // The User#getActivity() method returns an Optional
        //embed.addField("Activity", user.getActivity().map(Activity::getName).orElse("none"), true);
        //embed.addField("Activity", user.get, true);

        //});

        // Keep in mind that messages can also be sent as private messages
    /*event.getMessage().getServer()
            .ifPresent(server -> embed.addField("Server Admin", activeServer.isOwner(user) ? "yes" : "no", true));*/


        // Send the embed. It logs every exception, besides missing permissions (you are not allowed to send message in the channel)
        event.getChannel().sendMessage(embed)
                .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
    }
}