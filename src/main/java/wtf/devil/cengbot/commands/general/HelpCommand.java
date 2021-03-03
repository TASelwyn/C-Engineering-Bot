package wtf.devil.cengbot.commands.general;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.exception.MissingPermissionsException;
import org.javacord.api.util.logging.ExceptionLogger;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.utils.modules.Parsers;
import wtf.devil.cengbot.utils.objects.commandTypes;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand {
    public HelpCommand(MessageCreateEvent event, String[] params) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("C-Eng Bot Help");

        if (params.length >= 1) {
            String nonParsedCommand = params[0];
            String activeCommand = Parsers.parseStringToCommand(nonParsedCommand);
            commandTypes module = Parsers.parseStringToCommandModule(activeCommand);

            if (activeCommand != "") {
                String[] helpDocumentation = commandToHelpDocumentation(module, activeCommand);
                embed.addField("Basic Documentation for " + activeCommand, helpDocumentation[0], false);
                embed.addField("Example usage", helpDocumentation[1], false);
            } else {
                embed.addField("Command " + nonParsedCommand + " does not exist.", "It's hard to find documentation for a command that doesn't exist.", false);
            }
        } else {
            embed.addField("General Commands", listCommandsInModule(commandTypes.CORE), false);
            embed.addField("Economy Commands", listCommandsInModule(commandTypes.ECONOMY), false);
            embed.addField("Math Calculators", listCommandsInModule(commandTypes.CALCULATORS), false);
        }

        event.getChannel().sendMessage(embed)
                .exceptionally(ExceptionLogger.get(MissingPermissionsException.class));
    }

    private String listCommandsInModule(commandTypes commandModule) {
        String msg = "Module not listed";
        List<String> commandsListed = new ArrayList<>();

        for (int i = 0; i < Constants.modules.length; i++) {
            if (Constants.modules[i] == commandModule) {
                for (int j = 0; j < Constants.commands[i].length; j++) {
                    commandsListed.add(Constants.commands[i][j][0]);
                }
                msg = String.join(", ", commandsListed);
                break;
            }
        }

        return msg;
    }

    private String[] commandToHelpDocumentation(commandTypes commandModule, String command) {
        String[] helpDocumentation = {"", ""};

        loop:
        for (int i = 0; i < Constants.modules.length; i++) {
            if (Constants.modules[i] == commandModule) {
                for (int j = 0; j < Constants.commandsHelp[i].length; j++) {
                    if (Constants.commandsHelp[i][j][0].equalsIgnoreCase(command)) {
                        helpDocumentation[0] = Constants.commandsHelp[i][j][1];
                        helpDocumentation[1] = Constants.commandsHelp[i][j][2];
                        break loop;
                    }
                }
            }
        }

        return helpDocumentation;
    }
}

