package wtf.devil.cengbot.utils.database.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Session;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.utils.database.model.BotUser;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UserRepo extends CrudRepo<BotUser, Long> {
    private static final Logger logger = LogManager.getLogger(DevilsBot.class);

    public UserRepo() {
        super(BotUser.class);
    }

    public BotUser findOrCreate(long discordID) {
        return sessions().fromTransaction(session -> findOrCreate(session, discordID, LockMode.NONE));
    }

    // Loads the user with a row lock, applies the change and commits, so concurrent updates can't overwrite each other
    public void update(long discordID, Consumer<BotUser> change) {
        sessions().inTransaction(session -> change.accept(findOrCreate(session, discordID, LockMode.PESSIMISTIC_WRITE)));
    }

    // Same as update, but for changes that move something between two users (pay, rob) in one transaction
    public void update(long firstID, long secondID, BiConsumer<BotUser, BotUser> change) {
        sessions().inTransaction(session -> {
            // Always lock in id order so two opposite transfers can't deadlock
            BotUser lower = findOrCreate(session, Math.min(firstID, secondID), LockMode.PESSIMISTIC_WRITE);
            BotUser higher = findOrCreate(session, Math.max(firstID, secondID), LockMode.PESSIMISTIC_WRITE);
            boolean firstIsLower = firstID <= secondID;
            change.accept(firstIsLower ? lower : higher, firstIsLower ? higher : lower);
        });
    }

    public void updateNickname(long discordID, String nickname) {
        sessions().inTransaction(session -> {
            BotUser user = session.find(BotUser.class, discordID);
            if (user != null && !nickname.equals(user.getNickname())) {
                user.setNickname(nickname);
                logger.info("(" + discordID + "-DB) set nickname to " + nickname);
            }
        });
    }

    public List<BotUser> findRichest(int limit) {
        return sessions().fromSession(session -> session
                .createSelectionQuery("from BotUser order by cash desc, bank desc", BotUser.class)
                .setMaxResults(limit)
                .getResultList());
    }

    private static BotUser findOrCreate(Session session, long discordID, LockMode lockMode) {
        BotUser user = session.find(BotUser.class, discordID, lockMode);
        if (user == null) {
            user = new BotUser(discordID);
            session.persist(user);
            logger.info("(" + discordID + "-DB) user created");
        }
        return user;
    }
}
