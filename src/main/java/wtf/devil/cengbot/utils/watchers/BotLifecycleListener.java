package wtf.devil.cengbot.utils.watchers;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.commands.dev.WooclapClearCommand;
import wtf.devil.cengbot.commands.dev.WooclapPriorityCommand;
import wtf.devil.cengbot.commands.dev.WooclapPullCommand;
import wtf.devil.cengbot.commands.general.WooclapCommand;
import wtf.devil.cengbot.commands.general.WooclapQuestionsCommand;
import wtf.devil.cengbot.commands.general.WooclapStatusCommand;
import wtf.devil.cengbot.commands.general.WooclapTokenCommands;

public class BotLifecycleListener extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(DevilsBot.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Logged in as " + event.getJDA().getSelfUser().getName() + ", ready!");
        event.getJDA().updateCommands()
                .addCommands(WooclapCommand.SLASH_COMMAND)
                .addCommands(WooclapCommand.DROP_SLASH_COMMAND)
                .addCommands(WooclapStatusCommand.SLASH_COMMAND)
                .addCommands(WooclapQuestionsCommand.SLASH_COMMAND)
                .addCommands(WooclapPullCommand.SLASH_COMMAND)
                .addCommands(WooclapPriorityCommand.SLASH_COMMAND)
                .addCommands(WooclapTokenCommands.SLASH_COMMANDS)
                .queue();

        // Nobody is in an event after a restart, so start the log channel fresh
        WooclapClearCommand.clearLogChannel(event.getJDA(), "Wooclapper started. Use `/join <event_code>` to join a Wooclap event.");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals(WooclapCommand.SLASH_NAME)) {
            WooclapCommand.onSlashCommand(event);
        } else if (event.getName().equals(WooclapCommand.DROP_SLASH_NAME)) {
            WooclapCommand.onDropSlashCommand(event);
        } else if (event.getName().equals(WooclapStatusCommand.SLASH_NAME)) {
            WooclapStatusCommand.onSlashCommand(event);
        } else if (event.getName().equals(WooclapQuestionsCommand.SLASH_NAME)) {
            WooclapQuestionsCommand.onSlashCommand(event);
        } else if (event.getName().equals(WooclapPullCommand.SLASH_NAME)) {
            WooclapPullCommand.onSlashCommand(event);
        } else if (event.getName().equals(WooclapPriorityCommand.SLASH_NAME)) {
            WooclapPriorityCommand.onSlashCommand(event);
        } else {
            WooclapTokenCommands.onSlashCommand(event);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(WooclapCommand.TOKEN_BUTTON_ID)) {
            WooclapCommand.onTokenButton(event);
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals(WooclapCommand.TOKEN_MODAL_ID)) {
            WooclapCommand.onTokenModal(event);
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        logger.info("Joined server " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.info("Left server " + event.getGuild().getName());
    }
}
