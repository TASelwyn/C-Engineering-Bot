package wtf.devil.cengbot.commands.general;

import jakarta.persistence.PersistenceException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.selwyn.wooclapper.dto.User;
import tech.selwyn.wooclapper.dto.UserProfile;
import tech.selwyn.wooclapper.model.error.NeedsAuth;
import tech.selwyn.wooclapper.model.error.WooclapError;
import wtf.devil.cengbot.utils.database.model.WooclapToken;
import wtf.devil.cengbot.utils.database.repo.WooclapTokenRepo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/*
 * Slash commands for managing your saved Wooclap auth token.
 * Every reply is ephemeral, so these are safe to use in any channel.
 */
public class WooclapTokenCommands {

    private static final Logger logger = LogManager.getLogger(WooclapTokenCommands.class);
    private static final WooclapTokenRepo tokenRepo = new WooclapTokenRepo();

    public static final String SET_NAME = "settoken";
    public static final String GET_NAME = "gettoken";
    public static final String CLEAR_NAME = "cleartoken";
    public static final String CHECK_NAME = "checktoken";

    public static final List<SlashCommandData> SLASH_COMMANDS = List.of(
            Commands.slash(SET_NAME, "Save or replace your Wooclap auth token (only you can see this).")
                    .addOption(OptionType.STRING, "token", "Your Wooclap auth token", true),
            Commands.slash(GET_NAME, "Show your saved Wooclap auth token (only you can see this)."),
            Commands.slash(CLEAR_NAME, "Delete your saved Wooclap auth token."),
            Commands.slash(CHECK_NAME, "Check that your saved Wooclap auth token still works (only you can see this).")
    );

    private record CheckResult(String message, boolean askForToken) {}

    // Returns false if the event isn't one of these commands
    public static boolean onSlashCommand(SlashCommandInteractionEvent event) {
        long discordID = event.getUser().getIdLong();

        if (event.getName().equals(SET_NAME)) {
            setToken(event, discordID);
            return true;
        }
        if (event.getName().equals(CHECK_NAME)) {
            checkToken(event, discordID);
            return true;
        }

        String reply;
        try {
            reply = switch (event.getName()) {
                case GET_NAME -> getToken(discordID);
                case CLEAR_NAME -> clearToken(discordID);
                default -> null;
            };
        } catch (PersistenceException e) {
            logger.error("Wooclap token command /{} failed for {}", event.getName(), discordID, e);
            reply = "Couldn't reach the token database, try again later.";
        }

        if (reply == null) {
            return false;
        }
        event.reply(reply).setEphemeral(true).queue();
        return true;
    }

    private static void setToken(SlashCommandInteractionEvent event, long discordID) {
        OptionMapping tokenOption = event.getOption("token");
        String authToken = tokenOption == null ? "" : tokenOption.getAsString().trim();
        if (authToken.isEmpty()) {
            event.reply("Invalid usage. `/" + SET_NAME + " <token>`").setEphemeral(true).queue();
            return;
        }

        // Validating hits the network, so defer to avoid the 3 second interaction timeout
        event.deferReply(true).queue(hook ->
                CompletableFuture.supplyAsync(() -> WooclapCommand.saveTokenAndJoinPending(discordID, authToken))
                        .thenAccept(reply -> hook.editOriginal(reply).queue()));
    }

    private static void checkToken(SlashCommandInteractionEvent event, long discordID) {
        // Checking hits the network, so defer to avoid the 3 second interaction timeout
        event.deferReply(true).queue(hook ->
                CompletableFuture.supplyAsync(() -> check(discordID))
                        .thenAccept(result -> {
                            var action = hook.editOriginal(result.message());
                            if (result.askForToken()) {
                                action.setComponents(WooclapCommand.tokenButtonRow());
                            }
                            action.queue();
                        }));
    }

    private static CheckResult check(long discordID) {
        Optional<String> savedToken;
        try {
            savedToken = tokenRepo.findById(discordID).map(WooclapToken::getAuthToken);
        } catch (PersistenceException e) {
            logger.error("Failed to read Wooclap token for {}", discordID, e);
            return new CheckResult("Couldn't reach the token database, try again later.", false);
        }

        if (savedToken.isEmpty()) {
            return new CheckResult("You don't have a Wooclap token saved. Enter one below to save it.", true);
        }

        UserProfile profile;
        try {
            profile = WooclapCommand.validateToken(discordID, savedToken.get());
        } catch (NeedsAuth e) {
            return new CheckResult("Your saved Wooclap token no longer works: " + e.getMessage() + "\nEnter a new one below to replace it.", true);
        } catch (WooclapError e) {
            return new CheckResult("Couldn't check your token with Wooclap right now: " + e.getMessage() + "\nTry again in a bit.", false);
        }

        User user = profile.user();
        if (!profile.hasAccount() || user == null) {
            return new CheckResult("Your saved Wooclap token works, but it's anonymous, so events that require login will reject it.", false);
        }
        String sso = user.hasSsoAccount() && user.organization() != null ? " (SSO: " + user.organization() + ")" : "";
        return new CheckResult("Your saved Wooclap token works. It belongs to **" + user.displayName() + "**" + sso + ".", false);
    }

    private static String getToken(long discordID) {
        Optional<String> savedToken = tokenRepo.findById(discordID).map(WooclapToken::getAuthToken);
        return savedToken
                .map(token -> "Your saved Wooclap token: ||`" + token + "`||")
                .orElse("You don't have a Wooclap token saved. Use `/" + SET_NAME + "` to save one.");
    }

    private static String clearToken(long discordID) {
        return tokenRepo.deleteById(discordID)
                ? "Your Wooclap token has been deleted."
                : "You don't have a Wooclap token saved.";
    }
}
