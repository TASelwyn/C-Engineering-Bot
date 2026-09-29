package wtf.devil.cengbot.utils.objects;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.function.BiConsumer;

public record CommandDefinition(
        String name,
        List<String> aliases,
        commandTypes module,
        String description,
        String usage,
        BiConsumer<MessageReceivedEvent, String[]> handler
) {
}
