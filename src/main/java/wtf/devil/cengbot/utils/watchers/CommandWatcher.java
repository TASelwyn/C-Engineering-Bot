package wtf.devil.cengbot.utils.watchers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.commands.CommandManager;
import wtf.devil.cengbot.utils.objects.commandTypes;

import java.util.Calendar;

import static wtf.devil.cengbot.utils.modules.Parsers.*;

public class CommandWatcher extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        long startTimestamp = Calendar.getInstance().getTimeInMillis();
        //System.out.println("TIME DELTA: " + (Calendar.getInstance().getTimeInMillis() - event.getMessage().getTimeCreated().toInstant().toEpochMilli()));

        if (event.getMessage().getContentRaw().toLowerCase().startsWith(Constants.commandPrefix) && !event.getAuthor().isBot() && !event.isWebhookMessage()) {

            String[] msg = event.getMessage().getContentRaw().substring(Constants.commandPrefix.length()).trim().replaceAll(" +", " ").split(" ");

            String command = parseStringToCommand(msg[0].toLowerCase());
            String[] params = new String[msg.length - 1];

            /* Fill param with msg array starting at position 1 (cuts out command as a param) */
            for (int i = 0; (i < msg.length - 1); i++) {
                params[i] = msg[i + 1];
            }

            commandTypes commandModule = parseStringToCommandModule(command);

            if (!command.isEmpty() && commandModule != null) {
                new CommandManager(commandModule, event, command, params, startTimestamp);
            } else {
                event.getChannel().sendMessage("Unknown command. Check `c.help`").queue();
            }
        }
    }
}
