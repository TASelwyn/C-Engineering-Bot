package wtf.devil.cengbot.utils.modules;

import tech.selwyn.wooclapper.model.Wooclap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Which Wooclap events each Discord user is in, and the participant they were joined as.
 * Wooclapper only knows participants by UUID, so this is how the bot finds a user's live events.
 */
public final class WooclapParticipants {

    public record Membership(long discordID, String eventCode) {}

    private record Joined(UUID uuid, Wooclap wooclap) {}

    private static final Map<Membership, Joined> joined = new ConcurrentHashMap<>();

    private WooclapParticipants() {}

    // Call after a participant joins successfully
    public static void track(long discordID, String eventCode, UUID uuid, Wooclap wooclap) {
        joined.put(new Membership(discordID, eventCode), new Joined(uuid, wooclap));
    }

    /*
     * Stops tracking a participant. Call before removing them on purpose, so the removal isn't reported as a revoke.
     * Returns the event they were in, if they were tracked.
     */
    public static Optional<Wooclap> untrack(long discordID, String eventCode) {
        Joined entry = joined.remove(new Membership(discordID, eventCode));
        return Optional.ofNullable(entry).map(Joined::wooclap);
    }

    // Codes of the events the user is currently in
    public static List<String> eventsFor(long discordID) {
        return joined.keySet().stream()
                .filter(key -> key.discordID() == discordID)
                .map(Membership::eventCode)
                .sorted()
                .toList();
    }

    public record Member(long discordID, int priority) {}

    // Users in each event with their live priority, by event code. Skips anyone Wooclapper already removed but pruneRemoved() hasn't caught yet.
    public static Map<String, List<Member>> participantsByEvent() {
        Map<String, List<Member>> byEvent = new TreeMap<>();
        joined.forEach((key, entry) -> entry.wooclap().getParticipant(entry.uuid()).ifPresent(participant ->
                byEvent.computeIfAbsent(key.eventCode(), code -> new ArrayList<>()).add(new Member(key.discordID(), participant.getPriority()))));
        return byEvent;
    }

    // Changes the user's answer priority in every event they're in. Returns the codes it was changed in.
    public static List<String> setPriority(long discordID, int priority) {
        List<String> changed = new ArrayList<>();
        joined.forEach((key, entry) -> {
            if (key.discordID() != discordID) {
                return;
            }
            entry.wooclap().getParticipant(entry.uuid()).ifPresent(participant -> {
                participant.setPriority(priority);
                changed.add(key.eventCode());
            });
        });
        changed.sort(null);
        return changed;
    }

    /*
     * Stops tracking participants Wooclapper removed on its own (it drops them when their token is revoked),
     * and returns who they were. The bot untracks before removing anyone itself, so it never shows up here.
     */
    public static List<Membership> pruneRemoved() {
        List<Membership> removed = new ArrayList<>();
        joined.forEach((key, entry) -> {
            if (entry.wooclap().hasParticipant(entry.uuid())) {
                return;
            }
            // Only counts if this entry is still current, so a rejoin in the meantime isn't reported
            if (joined.remove(key, entry)) {
                removed.add(key);
            }
        });
        return removed;
    }
}
