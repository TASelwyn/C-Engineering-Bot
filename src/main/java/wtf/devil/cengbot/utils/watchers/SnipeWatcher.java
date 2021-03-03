package wtf.devil.cengbot.utils.watchers;

import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.listener.message.MessageEditListener;
import org.javacord.api.util.logging.ExceptionLogger;

import java.awt.*;
import java.sql.Time;
import java.util.Calendar;
import java.util.NoSuchElementException;

public class SnipeWatcher implements MessageEditListener {

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        // snipe watcher

        try {
            MessageAuthor author = event.getMessageAuthor().get();

            author.asUser().ifPresent(user -> {
                //event.getChannel().sendMessage("Hey, " + user.getMentionTag() + "!");

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Auto sniped")
                        .addField("Old message", event.getOldContent().get(), false)
                        .addField("New message", event.getNewContent(), false)
                        //.addField("Edit timestamp", event.getMessage().get().getLastEditTimestamp().get().toString(), false)
                        .setFooter(Calendar.getInstance().getTime().toString())
                        .setColor(Color.BLACK)
                        //.setAuthor("hi", "fuckyou", "https://cdn.discordapp.com/embed/avatars/0.png");
                        .setAuthor(author);



                event.getMessageId();

                event.getChannel().sendMessage(embed)
                        .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
            });
        } catch (NoSuchElementException e) {
            // do nothing because msg is too old to care
        }

    }
}
