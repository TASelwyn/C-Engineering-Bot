package wtf.devil.cengbot.utils.watchers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.selwyn.wooclapper.model.Wooclap;
import wtf.devil.cengbot.commands.general.WooclapCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * Notices when Wooclapper drops a participant because their token was revoked.
 * Wooclapper's sendAnswer removes them without telling anyone, and the bot never removes participants itself,
 * so a tracked participant that's gone from its event means the token stopped working.
 * The user is pinged in the Wooclap log channel and the event is queued, so entering a new token rejoins them.
 */
public final class WooclapRevokeWatcher {

    private static final Logger logger = LogManager.getLogger(WooclapRevokeWatcher.class);

    private static final long CHECK_INTERVAL_SECONDS = 10;

    private record Key(long discordID, String eventCode) {}

    private record Joined(UUID uuid, Wooclap wooclap) {}

    private static final Map<Key, Joined> joined = new ConcurrentHashMap<>();

    static {
        ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "wooclap-revoke-watcher");
            thread.setDaemon(true);
            return thread;
        });
        checker.scheduleWithFixedDelay(WooclapRevokeWatcher::check, CHECK_INTERVAL_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private WooclapRevokeWatcher() {}

    // Call after a participant joins successfully
    public static void track(long discordID, String eventCode, UUID uuid, Wooclap wooclap) {
        joined.put(new Key(discordID, eventCode), new Joined(uuid, wooclap));
    }

    /*
     * Stops watching a participant. Call before removing them on purpose, so the removal isn't reported as a revoke.
     * Returns the event they were in, if they were tracked.
     */
    public static Optional<Wooclap> untrack(long discordID, String eventCode) {
        Joined entry = joined.remove(new Key(discordID, eventCode));
        return Optional.ofNullable(entry).map(Joined::wooclap);
    }

    // Codes of the events the user is currently in
    public static List<String> eventsFor(long discordID) {
        return joined.keySet().stream()
                .filter(key -> key.discordID() == discordID)
                .map(Key::eventCode)
                .sorted()
                .toList();
    }

    // Users in each event, by event code. Skips anyone already removed but not yet noticed by check().
    public static Map<String, List<Long>> participantsByEvent() {
        Map<String, List<Long>> byEvent = new TreeMap<>();
        joined.forEach((key, entry) -> {
            if (entry.wooclap().hasParticipant(entry.uuid())) {
                byEvent.computeIfAbsent(key.eventCode(), code -> new ArrayList<>()).add(key.discordID());
            }
        });
        return byEvent;
    }

    private static void check() {
        joined.forEach((key, entry) -> {
            if (entry.wooclap().hasParticipant(entry.uuid())) {
                return;
            }
            // Only notify if this entry is still current, so a rejoin in the meantime isn't reported
            if (joined.remove(key, entry)) {
                notifyRevoked(key.discordID(), key.eventCode());
            }
        });
    }

    private static void notifyRevoked(long discordID, String eventCode) {
        logger.info("Wooclap token for {} was revoked in {}", discordID, eventCode);
        WooclapCommand.queuePendingCode(discordID, eventCode);
        WooclapCommand.pingForToken(List.of(discordID), "Wooclap stopped accepting your auth token, so you were removed from `" + eventCode
                + "`. Enter a new token below (or with `/settoken`) and I'll rejoin you.");
    }
}
