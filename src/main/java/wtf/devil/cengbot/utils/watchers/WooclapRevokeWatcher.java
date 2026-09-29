package wtf.devil.cengbot.utils.watchers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.commands.general.WooclapCommand;
import wtf.devil.cengbot.utils.modules.WooclapParticipants;
import wtf.devil.cengbot.utils.modules.WooclapParticipants.Membership;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * Notices when Wooclapper drops a participant because their token was revoked.
 * Wooclapper's sendAnswer removes them without telling anyone, so every few seconds this checks
 * WooclapParticipants for users who are gone from their event.
 * The user is pinged in the Wooclap log channel and the event is queued, so entering a new token rejoins them.
 */
public final class WooclapRevokeWatcher {

    private static final Logger logger = LogManager.getLogger(WooclapRevokeWatcher.class);

    private static final long CHECK_INTERVAL_SECONDS = 10;

    private static final AtomicBoolean started = new AtomicBoolean();

    private WooclapRevokeWatcher() {}

    // Starts checking in the background. Only the first call does anything.
    public static void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "wooclap-revoke-watcher");
            thread.setDaemon(true);
            return thread;
        });
        checker.scheduleWithFixedDelay(WooclapRevokeWatcher::check, CHECK_INTERVAL_SECONDS, CHECK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private static void check() {
        for (Membership removed : WooclapParticipants.pruneRemoved()) {
            notifyRevoked(removed.discordID(), removed.eventCode());
        }
    }

    private static void notifyRevoked(long discordID, String eventCode) {
        logger.info("Wooclap token for {} was revoked in {}", discordID, eventCode);
        WooclapCommand.queuePendingCode(discordID, eventCode);
        WooclapCommand.pingForToken(List.of(discordID), "Wooclap stopped accepting your auth token, so you were removed from `" + eventCode
                + "`. Enter a new token below (or with `/settoken`) and I'll rejoin you.");
    }
}
