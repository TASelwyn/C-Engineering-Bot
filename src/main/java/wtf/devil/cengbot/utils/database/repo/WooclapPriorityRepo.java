package wtf.devil.cengbot.utils.database.repo;

import tech.selwyn.wooclapper.model.participants.Participant;
import wtf.devil.cengbot.utils.database.model.WooclapPriority;

public class WooclapPriorityRepo extends CrudRepo<WooclapPriority, Long> {

    public WooclapPriorityRepo() {
        super(WooclapPriority.class);
    }

    // The user's saved priority, or Wooclapper's default if they don't have one
    public int priorityFor(long discordId) {
        return findById(discordId).map(WooclapPriority::getPriority).orElse(Participant.DEFAULT_PRIORITY);
    }
}
