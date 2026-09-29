package wtf.devil.cengbot.commands.dev;

import jakarta.persistence.PersistenceException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.selwyn.wooclapper.model.participants.Participant;
import wtf.devil.cengbot.commands.CommandManager;
import wtf.devil.cengbot.utils.database.model.WooclapPriority;
import wtf.devil.cengbot.utils.database.repo.WooclapPriorityRepo;
import wtf.devil.cengbot.utils.modules.WooclapParticipants;

import java.util.List;
import java.util.stream.Collectors;

/*
 * Sets the order users answer Wooclap questions in. Lower answers first, equal priorities answer together.
 * Saved per user, used for every event they join, and applied right away to the events they're already in.
 *   /setpriority <name> <priority>
 */
public class WooclapPriorityCommand {

    private static final Logger logger = LogManager.getLogger(WooclapPriorityCommand.class);
    private static final WooclapPriorityRepo priorityRepo = new WooclapPriorityRepo();

    public static final String SLASH_NAME = "setpriority";

    // Hidden from non-admins in the command list; onSlashCommand checks again since server settings can override that
    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Set someone's Wooclap answer order, lower answers first (DEV only).")
            .addOption(OptionType.USER, "name", "Whose priority to set", true)
            .addOptions(new OptionData(OptionType.INTEGER, "priority", "Lower answers first, equal answer together. Default is " + Participant.DEFAULT_PRIORITY + ".", true)
                    .setRequiredRange(Integer.MIN_VALUE, Integer.MAX_VALUE))
            .setContexts(InteractionContextType.GUILD)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        if (!CommandManager.isDev(event.getMember())) {
            event.reply("No permissions.").setEphemeral(true).queue();
            return;
        }

        OptionMapping nameOption = event.getOption("name");
        OptionMapping priorityOption = event.getOption("priority");
        if (nameOption == null || priorityOption == null) {
            event.reply("Invalid usage. `/setpriority <name> <priority>`").setEphemeral(true).queue();
            return;
        }

        User user = nameOption.getAsUser();
        if (user.isBot()) {
            event.reply("Bots can't join Wooclap events.").setEphemeral(true).queue();
            return;
        }

        long discordID = user.getIdLong();
        int priority = priorityOption.getAsInt();

        try {
            priorityRepo.save(new WooclapPriority(discordID, priority));
        } catch (PersistenceException e) {
            logger.error("Failed to save Wooclap priority for {}", discordID, e);
            event.reply("Couldn't save the priority, try again later.").setEphemeral(true).queue();
            return;
        }

        // Saved first, so a join happening now picks up the new priority too
        List<String> changed = WooclapParticipants.setPriority(discordID, priority);
        logger.info("Set Wooclap priority for {} to {}, updated in {}", discordID, priority, changed);

        String events = changed.isEmpty()
                ? "They're not in any events right now, so it'll apply when they next join."
                : "Updated in " + changed.stream().map(code -> "`" + code + "`").collect(Collectors.joining(", ")) + ".";
        event.reply("Set <@" + discordID + ">'s Wooclap priority to **" + priority + "**. " + events)
                // Names them without a ping
                .setAllowedMentions(List.of())
                .queue();
    }
}
