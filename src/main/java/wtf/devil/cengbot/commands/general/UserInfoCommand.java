package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.MessageUtils;

import java.util.List;

public class UserInfoCommand {
    public UserInfoCommand(MessageReceivedEvent event, String[] params) {
        Guild activeServer = event.getGuild();
        User user = event.getAuthor();
        Member member = event.getMember();

        // Grabs a mention if there is one, if not it'll just default to the message author.
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
        if (!mentionedUsers.isEmpty()) {
            user = mentionedUsers.get(0);
            List<Member> mentionedMembers = event.getMessage().getMentions().getMembers();
            member = mentionedMembers.isEmpty() ? activeServer.getMember(user) : mentionedMembers.get(0);
        }

        String displayName = member != null ? member.getEffectiveName() : user.getName();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("User Info")
                .addField("Display Name", displayName, true)
                .addField("Name", user.getName(), true)
                .addField("User Id", user.getId(), true)
                .setAuthor(user.getName(), null, user.getEffectiveAvatarUrl());
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
        event.getChannel().sendMessageEmbeds(embed.build())
                .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
    }
}
