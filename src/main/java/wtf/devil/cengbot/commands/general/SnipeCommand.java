package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.MessageUtils;

import java.awt.*;

public class SnipeCommand {
    public SnipeCommand(MessageReceivedEvent event, String[] params) {
        //event.getChannel().sendMessage("EXAMPLE COMMAND");
        User author = event.getAuthor();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("COMMAND NOT FUNCTIONAL YET --->")
                .addField("Old message", "a msg", false)
                .addField("New message", "not a command yet", false)
                //.addField("Edit timestamp", event.getMessage().get().getLastEditTimestamp().get().toString(), false)
                .setColor(Color.BLACK)
                .setAuthor(author.getName(), null, author.getEffectiveAvatarUrl());

        event.getChannel().sendMessageEmbeds(embed.build())
                .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
    }
}
