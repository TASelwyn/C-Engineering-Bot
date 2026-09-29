package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.commands.CommandRegistry;
import wtf.devil.cengbot.utils.MessageUtils;
import wtf.devil.cengbot.utils.objects.CommandDefinition;
import wtf.devil.cengbot.utils.objects.commandTypes;

import java.util.Optional;
import java.util.stream.Collectors;

public class HelpCommand {
    public HelpCommand(MessageReceivedEvent event, String[] params) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("C-Eng Bot Help");

        if (params.length >= 1) {
            Optional<CommandDefinition> definition = CommandRegistry.find(params[0]);

            if (definition.isPresent()) {
                embed.addField("Basic Documentation for " + definition.get().name(), definition.get().description(), false);
                embed.addField("Example usage", definition.get().usage(), false);
            } else {
                embed.addField("Command " + params[0] + " does not exist.", "It's hard to find documentation for a command that doesn't exist.", false);
            }
        } else {
            embed.addField("General Commands", listCommandsInModule(commandTypes.CORE), false);
            embed.addField("Economy Commands", listCommandsInModule(commandTypes.ECONOMY), false);
            embed.addField("Math Calculators", listCommandsInModule(commandTypes.CALCULATORS), false);
        }

        event.getChannel().sendMessageEmbeds(embed.build())
                .queue(null, MessageUtils.IGNORE_MISSING_PERMS);
    }

    private String listCommandsInModule(commandTypes module) {
        return CommandRegistry.inModule(module).stream()
                .map(CommandDefinition::name)
                .collect(Collectors.joining(", "));
    }
}
