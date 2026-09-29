package wtf.devil.cengbot.commands.general;

import jakarta.persistence.PersistenceException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.selwyn.wooclapper.Wooclapper;
import tech.selwyn.wooclapper.dto.UserProfile;
import tech.selwyn.wooclapper.model.Wooclap;
import tech.selwyn.wooclapper.model.error.NeedsAuth;
import tech.selwyn.wooclapper.model.error.WooclapError;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.commands.CommandManager;
import wtf.devil.cengbot.utils.database.model.WooclapToken;
import wtf.devil.cengbot.utils.database.repo.WooclapPriorityRepo;
import wtf.devil.cengbot.utils.database.repo.WooclapTokenRepo;
import wtf.devil.cengbot.utils.modules.Parsers;
import wtf.devil.cengbot.utils.modules.WooclapLogRouter;
import wtf.devil.cengbot.utils.modules.WooclapParticipants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static wtf.devil.cengbot.Constants.wooclapLogChannelId;

public class WooclapCommand {

    private static final Logger logger = LogManager.getLogger(WooclapCommand.class);
    private static final WooclapTokenRepo tokenRepo = new WooclapTokenRepo();
    private static final WooclapPriorityRepo priorityRepo = new WooclapPriorityRepo();

    public static final String SLASH_NAME = "join";
    public static final String TOKEN_BUTTON_ID = "wooclap:enter-token";
    public static final String TOKEN_MODAL_ID = "wooclap:token-modal";
    private static final String TOKEN_INPUT_ID = "token";

    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Join a Wooclap event with your saved token.")
            .addOption(OptionType.STRING, "code", "The Wooclap event code", true);

    public static final String DROP_SLASH_NAME = "drop";

    public static final SlashCommandData DROP_SLASH_COMMAND = Commands.slash(DROP_SLASH_NAME, "Stop answering a Wooclap event for you (only you can see this).")
            .addOption(OptionType.STRING, "code", "The Wooclap event code. Leave empty to drop out of every event.", false)
            .addOption(OptionType.STRING, "users", "People to drop, as @mentions or user IDs (DEV only for anyone but you)", false);

    public enum DropStatus { DROPPED, CANCELLED, NOT_IN }

    // Event code each user tried to join without a working token, joined automatically once they enter one
    private static final Map<Long, String> pendingCodes = new ConcurrentHashMap<>();

    private enum JoinStatus { JOINED, FAILED, TOKEN_FAILED }

    private record JoinResult(JoinStatus status, String message) {}

    /*
     * Text command:
     *   c.join <code>          - anywhere, joins using the user's saved token (c.wooclap works too)
     *   c.join <code> <token>  - DMs only, saves the token and joins
     */
    public WooclapCommand(MessageReceivedEvent event, String[] params) {
        if (params.length == 2 && event.isFromGuild()) {
            event.getMessage().delete().queue(null, error -> {});
            event.getChannel().sendMessage(event.getAuthor().getAsMention()
                    + " don't post your Wooclap token in a server! Use `/settoken` (only you can see it) or DM me `c.join <event_code> <token>`.").queue();
            return;
        }

        if (params.length != 1 && params.length != 2) {
            event.getChannel().sendMessage("Invalid usage. `c.help join`").queue();
            return;
        }

        long callerDiscordID = event.getAuthor().getIdLong();
        String eventCode = params[0].toUpperCase();
        String mention = event.isFromGuild() ? event.getAuthor().getAsMention() + " " : "";

        if (params.length == 2) {
            CompletableFuture.supplyAsync(() -> joinAndSave(callerDiscordID, eventCode, params[1]))
                    .thenAccept(reply -> event.getChannel().sendMessage(mention + reply).queue());
            return;
        }

        CompletableFuture.supplyAsync(() -> joinWithSavedToken(callerDiscordID, eventCode))
                .thenAccept(result -> {
                    var action = event.getChannel().sendMessage(mention + result.message());
                    if (result.status() == JoinStatus.TOKEN_FAILED) {
                        action.setComponents(tokenButtonRow());
                    }
                    action.queue();
                });
    }

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        OptionMapping codeOption = event.getOption("code");
        if (codeOption == null) {
            event.reply("Invalid usage. `/join <code>`").setEphemeral(true).queue();
            return;
        }

        long callerDiscordID = event.getUser().getIdLong();
        String eventCode = codeOption.getAsString().trim().toUpperCase();

        // Joining hits the network, so defer to avoid the 3 second interaction timeout
        event.deferReply(true).queue(hook ->
                CompletableFuture.supplyAsync(() -> joinWithSavedToken(callerDiscordID, eventCode))
                        .thenAccept(result -> {
                            var action = hook.editOriginal(result.message());
                            if (result.status() == JoinStatus.TOKEN_FAILED) {
                                action.setComponents(tokenButtonRow());
                            }
                            action.queue();
                        }));
    }

    public static void onDropSlashCommand(SlashCommandInteractionEvent event) {
        long callerDiscordID = event.getUser().getIdLong();
        OptionMapping codeOption = event.getOption("code");
        OptionMapping usersOption = event.getOption("users");

        if (usersOption != null) {
            Set<Long> targets = Parsers.parseUserIds(usersOption.getAsString(), usersOption.getMentions());
            if (targets.isEmpty()) {
                event.reply("I couldn't find anyone in `users`. @mention them or paste their user IDs.").setEphemeral(true).queue();
                return;
            }
            if (!targets.equals(Set.of(callerDiscordID))) {
                dropOthers(event, targets, codeOption);
                return;
            }
        }

        if (codeOption != null) {
            String eventCode = codeOption.getAsString().trim().toUpperCase();
            String reply = switch (drop(callerDiscordID, eventCode)) {
                case DROPPED -> "Dropped out of Wooclap `" + eventCode + "`. I'll stop answering it for you.";
                case CANCELLED -> "Cancelled your queued join for Wooclap `" + eventCode + "`.";
                case NOT_IN -> "You're not in Wooclap `" + eventCode + "`.";
            };
            event.reply(reply).setEphemeral(true).queue();
            return;
        }

        List<String> dropped = dropAll(callerDiscordID);
        String reply = dropped.isEmpty()
                ? "You're not in any Wooclap events."
                : "Dropped out of " + dropped.stream().map(code -> "`" + code + "`").collect(Collectors.joining(", ")) + ".";
        event.reply(reply).setEphemeral(true).queue();
    }

    // DEV only. Posted in the channel like the text command, one line per user, without pinging them.
    private static void dropOthers(SlashCommandInteractionEvent event, Set<Long> targets, OptionMapping codeOption) {
        if (!CommandManager.isDev(event.getMember())) {
            event.reply("Only DEVs can drop other people. Leave out `users` to drop yourself.").setEphemeral(true).queue();
            return;
        }

        String eventCode = codeOption == null ? null : codeOption.getAsString().trim().toUpperCase();
        StringBuilder reply = new StringBuilder(eventCode == null
                ? "Wooclap drop results (every event):"
                : "Wooclap `" + eventCode + "` drop results:");

        for (long discordID : targets) {
            String result;
            if (eventCode != null) {
                result = switch (drop(discordID, eventCode)) {
                    case DROPPED -> "Dropped.";
                    case CANCELLED -> "Cancelled their queued join.";
                    case NOT_IN -> "Wasn't in this event.";
                };
            } else {
                List<String> dropped = dropAll(discordID);
                result = dropped.isEmpty()
                        ? "Wasn't in any events."
                        : "Dropped out of " + dropped.stream().map(code -> "`" + code + "`").collect(Collectors.joining(", ")) + ".";
            }
            reply.append("\n<@").append(discordID).append(">: ").append(result);
        }

        event.reply(reply.toString()).setAllowedMentions(List.of()).queue();
    }

    /*
     * Stops answering an event for the user, and cancels a queued join for it so a new token won't rejoin them.
     * Untracked before removing, so the revoke watcher doesn't mistake the drop for a revoked token.
     */
    public static DropStatus drop(long discordID, String eventCode) {
        boolean cancelled = pendingCodes.remove(discordID, eventCode);

        Optional<Wooclap> wooclap = WooclapParticipants.untrack(discordID, eventCode);
        if (wooclap.isEmpty()) {
            return cancelled ? DropStatus.CANCELLED : DropStatus.NOT_IN;
        }

        wooclap.get().removeParticipant(uuidFromDiscordID(discordID));
        logger.info("{} dropped out of Wooclap {}", discordID, eventCode);
        return DropStatus.DROPPED;
    }

    // Drops the user out of every event they're in or queued for. Returns the codes they were dropped from.
    public static List<String> dropAll(long discordID) {
        List<String> dropped = new ArrayList<>();
        String pending = pendingCodes.remove(discordID);
        if (pending != null) {
            dropped.add(pending);
        }
        for (String eventCode : WooclapParticipants.eventsFor(discordID)) {
            if (drop(discordID, eventCode) == DropStatus.DROPPED && !dropped.contains(eventCode)) {
                dropped.add(eventCode);
            }
        }
        return dropped;
    }

    // Opens a private text box for the token. Whoever clicks enters their own token, so the button is safe in servers.
    public static void onTokenButton(ButtonInteractionEvent event) {
        TextInput tokenInput = TextInput.create(TOKEN_INPUT_ID, TextInputStyle.SHORT)
                .setPlaceholder("Your Wooclap auth token")
                .setRequired(true)
                .build();
        Modal modal = Modal.create(TOKEN_MODAL_ID, "Enter your Wooclap token")
                .addComponents(Label.of("Auth token", tokenInput))
                .build();
        event.replyModal(modal).queue();
    }

    public static void onTokenModal(ModalInteractionEvent event) {
        ModalMapping tokenValue = event.getValue(TOKEN_INPUT_ID);
        String authToken = tokenValue == null ? "" : tokenValue.getAsString().trim();
        if (authToken.isEmpty()) {
            event.reply("No token entered.").setEphemeral(true).queue();
            return;
        }

        long callerDiscordID = event.getUser().getIdLong();
        event.deferReply(true).queue(hook ->
                CompletableFuture.supplyAsync(() -> saveTokenAndJoinPending(callerDiscordID, authToken))
                        .thenAccept(reply -> hook.editOriginal(reply).queue()));
    }

    /*
     * Checks and saves a token, then joins the event the user was waiting on, if any.
     * Used by the token text box and /settoken.
     */
    public static String saveTokenAndJoinPending(long discordID, String authToken) {
        UserProfile profile;
        try {
            profile = validateToken(discordID, authToken);
        } catch (NeedsAuth e) {
            return "That Wooclap token didn't work, so it wasn't saved: " + e.getMessage();
        } catch (WooclapError e) {
            return "Couldn't check your token with Wooclap right now, so it wasn't saved: " + e.getMessage() + "\nTry again in a bit.";
        }

        try {
            tokenRepo.save(new WooclapToken(discordID, authToken));
        } catch (PersistenceException e) {
            logger.error("Failed to save Wooclap token for {}", discordID, e);
            return "Your Wooclap token is valid, but I couldn't save it. Try again later.";
        }

        // Anonymous tokens work, but events that require login will reject them
        String account = profile.hasAccount() && profile.user() != null
                ? "for **" + profile.user().displayName() + "**"
                : "(anonymous, so events that require login will reject it)";
        String reply = "Wooclap token verified and saved " + account + ".";

        String eventCode = pendingCodes.get(discordID);
        if (eventCode == null) {
            return reply + " You can now use `/join <event_code>` in any channel.";
        }

        JoinResult result = join(discordID, eventCode, authToken);
        return reply + "\n" + result.message();
    }

    private static JoinResult joinWithSavedToken(long discordID, String eventCode) {
        Optional<String> savedToken;
        try {
            savedToken = tokenRepo.findById(discordID).map(WooclapToken::getAuthToken);
        } catch (PersistenceException e) {
            logger.error("Failed to read Wooclap token for {}", discordID, e);
            return new JoinResult(JoinStatus.FAILED, "Couldn't look up your Wooclap token, try again later.");
        }

        if (savedToken.isEmpty()) {
            queuePendingCode(discordID, eventCode);
            return new JoinResult(JoinStatus.TOKEN_FAILED, "I don't have your Wooclap token saved. Enter it below and I'll join `" + eventCode + "` for you.");
        }

        JoinResult result = join(discordID, eventCode, savedToken.get());
        if (result.status() == JoinStatus.TOKEN_FAILED) {
            return new JoinResult(JoinStatus.TOKEN_FAILED, result.message()
                    + "\nIf your token changed, enter the new one below and I'll join `" + eventCode + "` for you.");
        }
        return result;
    }

    public enum PullStatus { JOINED, NO_TOKEN, TOKEN_REJECTED, FAILED }

    public record PullResult(String message, PullStatus status) {}

    /*
     * Joins someone else into an event with their saved token, for the DEV pull command.
     * Users without a saved token get the event queued, so they're joined once they enter one.
     */
    public static PullResult pullWithSavedToken(long discordID, String eventCode) {
        Optional<String> savedToken;
        try {
            savedToken = tokenRepo.findById(discordID).map(WooclapToken::getAuthToken);
        } catch (PersistenceException e) {
            logger.error("Failed to read Wooclap token for {}", discordID, e);
            return new PullResult("Couldn't look up their token.", PullStatus.FAILED);
        }

        if (savedToken.isEmpty()) {
            queuePendingCode(discordID, eventCode);
            return new PullResult("No saved token, asked them to set one.", PullStatus.NO_TOKEN);
        }

        // join queues the code itself when the token is rejected
        JoinResult result = join(discordID, eventCode, savedToken.get());
        PullStatus status = switch (result.status()) {
            case JOINED -> PullStatus.JOINED;
            case TOKEN_FAILED -> PullStatus.TOKEN_REJECTED;
            case FAILED -> PullStatus.FAILED;
        };
        String message = status == PullStatus.TOKEN_REJECTED
                ? result.message() + " Asked them for a new token in <#" + wooclapLogChannelId + ">."
                : result.message();
        return new PullResult(message, status);
    }

    /*
     * Pings users in the Wooclap log channel with the Enter token button.
     * Their events should already be queued, so entering a token joins them.
     */
    public static void pingForToken(Collection<Long> discordIDs, String message) {
        JDA jda = DevilsBot.getJda();
        MessageChannel channel = jda == null ? null : jda.getChannelById(MessageChannel.class, wooclapLogChannelId);
        if (channel == null) {
            logger.warn("Wooclap log channel {} is unavailable, couldn't ask {} for a token", wooclapLogChannelId, discordIDs);
            return;
        }

        String mentions = discordIDs.stream().map(id -> "<@" + id + ">").collect(Collectors.joining(" "));
        channel.sendMessage(mentions + " " + message)
                .setComponents(tokenButtonRow())
                // Log output after this starts a new message below the ping instead of editing one above it
                .queue(sent -> WooclapLogRouter.startNewMessage(),
                        error -> logger.warn("Failed to ask {} for a Wooclap token", discordIDs, error));
    }

    // Only saves the token once it's been proven to work
    private static String joinAndSave(long discordID, String eventCode, String authToken) {
        JoinResult result = join(discordID, eventCode, authToken);
        if (result.status() != JoinStatus.JOINED) {
            return result.message();
        }

        try {
            tokenRepo.save(new WooclapToken(discordID, authToken));
        } catch (PersistenceException e) {
            logger.error("Failed to save Wooclap token for {}", discordID, e);
            return result.message() + " Couldn't save your token for next time, though.";
        }
        return result.message() + " Your token is saved, so next time you can just use `/join <event_code>` in any channel.";
    }

    /*
     * Joins the event with the token. A failed join because of the token queues the code,
     * so it's joined automatically once the user enters a working token.
     */
    private static JoinResult join(long discordID, String eventCode, String authToken) {
        // Registered up front so the token is scrubbed from Wooclapper's output while joining too
        WooclapLogRouter.registerToken(authToken);

        Wooclap wooclap;
        try {
            wooclap = loadWooclap(eventCode);
        } catch (WooclapError e) {
            logger.warn("Failed to load Wooclap {} for {}: {}", eventCode, discordID, e.getMessage());
            return new JoinResult(JoinStatus.FAILED, "Couldn't load Wooclap `" + eventCode + "`: " + e.getMessage());
        }

        int priority;
        try {
            priority = priorityRepo.priorityFor(discordID);
        } catch (PersistenceException e) {
            logger.error("Failed to read Wooclap priority for {}", discordID, e);
            return new JoinResult(JoinStatus.FAILED, "Couldn't look up your Wooclap priority, try again later.");
        }

        try {
            wooclap.addParticipant(uuidFromDiscordID(discordID), authToken, priority);
        } catch (NeedsAuth e) {
            // Only a rejected token (or the wrong kind of account) is worth asking for a new one
            logger.warn("Wooclap {} rejected the token for {}: {}", eventCode, discordID, e.getMessage());
            queuePendingCode(discordID, eventCode);
            return new JoinResult(JoinStatus.TOKEN_FAILED, "Couldn't join Wooclap `" + eventCode + "`: " + e.getMessage());
        } catch (WooclapError e) {
            logger.warn("Failed to join Wooclap {} for {}: {}", eventCode, discordID, e.getMessage());
            return new JoinResult(JoinStatus.FAILED, "Couldn't join Wooclap `" + eventCode + "`: " + e.getMessage()
                    + "\nThis doesn't look like a token problem. Wooclap may be having issues, so try again in a bit.");
        }

        pendingCodes.remove(discordID, eventCode);
        WooclapParticipants.track(discordID, eventCode, uuidFromDiscordID(discordID), wooclap);
        return new JoinResult(JoinStatus.JOINED, "Joined Wooclap `" + eventCode + "`.");
    }

    // Users waiting to enter a token, by the event code they'll be joined to
    public static Map<String, List<Long>> pendingByEvent() {
        Map<String, List<Long>> byEvent = new TreeMap<>();
        pendingCodes.forEach((discordID, eventCode) -> byEvent.computeIfAbsent(eventCode, code -> new ArrayList<>()).add(discordID));
        return byEvent;
    }

    // Queues the code so it's joined automatically once the user enters a working token
    public static void queuePendingCode(long discordID, String eventCode) {
        pendingCodes.put(discordID, eventCode);
    }

    public static ActionRow tokenButtonRow() {
        return ActionRow.of(Button.primary(TOKEN_BUTTON_ID, "Enter token"));
    }

    /*
     * Checks which Wooclap account a token belongs to, without joining any event.
     * Throws NeedsAuth if Wooclap rejects the token, or WooclapError if the check fails.
     */
    public static UserProfile validateToken(long discordID, String authToken) throws WooclapError {
        WooclapLogRouter.registerToken(authToken);

        try {
            return DevilsBot.getWooclapper().fetchProfile(authToken);
        } catch (WooclapError e) {
            logger.warn("Wooclap token validation failed for {}: {}", discordID, e.getMessage());
            throw e;
        }
    }

    private static Wooclap loadWooclap(String eventCode) {
        Wooclapper wooclapper = DevilsBot.getWooclapper();
        // addWooclapByCode is a no-op for loaded events and throws the real reason on failure,
        // unlike getWooclapByCode which swallows it
        wooclapper.addWooclapByCode(eventCode);
        return wooclapper.getWooclapByCode(eventCode).orElseThrow(() -> new WooclapError("Wooclap " + eventCode + " could not be loaded."));
    }

    // Deterministic name-based UUID so the same Discord user always maps to the same participant
    private static UUID uuidFromDiscordID(long discordID) {
        return UUID.nameUUIDFromBytes(("discord:" + discordID).getBytes(StandardCharsets.UTF_8));
    }
}
