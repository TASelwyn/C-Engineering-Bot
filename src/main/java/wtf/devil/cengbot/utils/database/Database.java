package wtf.devil.cengbot.utils.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernatePersistenceConfiguration;
import org.hibernate.tool.schema.Action;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.utils.database.model.BotUser;
import wtf.devil.cengbot.utils.database.model.WooclapPriority;
import wtf.devil.cengbot.utils.database.model.WooclapToken;
import wtf.devil.cengbot.utils.objects.BotConfig;

// Owns the Hibernate SessionFactory (and its HikariCP pool) for the whole bot
public final class Database {
    private static final Logger logger = LogManager.getLogger(DevilsBot.class);

    private static SessionFactory sessionFactory;

    private Database() {
    }

    public static boolean connect(BotConfig config) {
        try {
            sessionFactory = new HibernatePersistenceConfiguration("cengbot")
                    .managedClasses(BotUser.class, WooclapToken.class, WooclapPriority.class)
                    .jdbcUrl(config.getDatabaseUrl())
                    .jdbcCredentials(config.getDatabaseUser(), config.getDatabasePassword())
                    // Creates missing tables/columns on startup, never drops anything
                    .schemaToolingAction(Action.UPDATE)
                    .property("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
                    .property("hibernate.hikari.maximumPoolSize", "5")
                    .createEntityManagerFactory();

            long users = sessionFactory.fromSession(session ->
                    session.createSelectionQuery("select count(*) from BotUser", Long.class).getSingleResult());
            logger.info("Connected to " + config.getDatabaseUrl() + ". Found " + users + " users in the database.");
            return true;
        } catch (RuntimeException e) {
            logger.error("Failed to connect to " + config.getDatabaseUrl(), e);
            return false;
        }
    }

    public static SessionFactory sessions() {
        if (sessionFactory == null) {
            throw new IllegalStateException("Database.connect() has not been called");
        }
        return sessionFactory;
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
