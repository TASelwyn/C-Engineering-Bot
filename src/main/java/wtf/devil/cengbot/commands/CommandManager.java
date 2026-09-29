package wtf.devil.cengbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import wtf.devil.cengbot.utils.database.repo.UserRepo;
import wtf.devil.cengbot.utils.modules.Economy;
import wtf.devil.cengbot.utils.objects.CommandDefinition;

import static wtf.devil.cengbot.utils.objects.commandTypes.OWNER;

public class CommandManager {

    private static final UserRepo userRepo = new UserRepo();

    public CommandManager(CommandDefinition definition, MessageReceivedEvent event, String[] params) {
        // Owner check needs the application info from Discord, so it runs async
        if (definition.module() == OWNER) {
            event.getJDA().retrieveApplicationInfo().queue(info -> {
                if (info.getOwner().getIdLong() == event.getAuthor().getIdLong()) {
                    definition.handler().accept(event, params);
                } else {
                    event.getChannel().sendMessage("No permissions.").queue();
                }
            });
            return;
        }

        if (isAllowed(definition, event)) {
            definition.handler().accept(event, params);
        }
    }

    private boolean isAllowed(CommandDefinition definition, MessageReceivedEvent event) {
        return switch (definition.module()) {
            case ECONOMY -> allowEconomy(event);
            case DEV -> allowDev(definition, event);
            case CORE, CALCULATORS -> true;
            case OWNER -> false; // handled in the constructor
        };
    }

    private boolean allowEconomy(MessageReceivedEvent event) {
        long authorId = event.getAuthor().getIdLong();

        if (!new Economy().healthCheck(authorId)) {
            event.getChannel().sendMessage("Economy commands are only available in <#799083105184382997>").queue();
            return false;
        }

        String currentName = event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName();
        userRepo.updateNickname(authorId, currentName);

        return true;
    }

    private boolean allowDev(CommandDefinition definition, MessageReceivedEvent event) {
        if (isDev(event.getMember()) || definition.name().equals("test")) {
            return true;
        }

        event.getChannel().sendMessage("No permissions.").queue();
        return false;
    }

    // DEV commands are for server admins. Null (e.g. in DMs) is never DEV.
    public static boolean isDev(Member member) {
        return member != null && member.hasPermission(Permission.ADMINISTRATOR);
    }
}
