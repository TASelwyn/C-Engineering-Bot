package wtf.devil.cengbot.utils.watchers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.commands.CommandManager;
import wtf.devil.cengbot.commands.CommandRegistry;
import wtf.devil.cengbot.utils.objects.CommandDefinition;

import java.util.Optional;

public class CommandWatcher extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();

        if (!content.toLowerCase().startsWith(Constants.commandPrefix) || event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String[] msg = content.substring(Constants.commandPrefix.length()).trim().replaceAll(" +", " ").split(" ");
        String[] params = new String[msg.length - 1];
        System.arraycopy(msg, 1, params, 0, params.length);

        Optional<CommandDefinition> definition = CommandRegistry.find(msg[0]);

        if (definition.isPresent()) {
            new CommandManager(definition.get(), event, params);
        } else {
            event.getChannel().sendMessage("Unknown command. Check `c.help`").queue();
        }
    }
}
