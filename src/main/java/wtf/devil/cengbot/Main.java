package wtf.devil.cengbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import wtf.devil.cengbot.utils.Config;
import wtf.devil.cengbot.utils.watchers.CommandWatcher;

import java.util.Calendar;

import static wtf.devil.cengbot.Constants.*;
import static wtf.devil.cengbot.utils.database.DatabaseManager.databaseTestConnection;

public class Main {

    private static Logger logger;

    public static void main(String[] args) {
        setLogProperty();

        String discordToken = new Config().getToken();

        //.setRecommendedTotalShards().join()
        if (databaseTestConnection()) {
            if ((discordToken != null) && discordToken.length() == 59) {
                new DiscordApiBuilder()
                        .setToken(discordToken)
                        .setAllIntents()

                        .setTotalShards(2)
                        //.loginAllShards()
                        .loginShards(0)

                        .forEach(shardFuture -> shardFuture
                                .thenAcceptAsync(Main::onShardLogin)
                                .exceptionally(ExceptionLogger.get())
                        );

                logger.info("Bot successfully started.");

            } else if (!discordToken.isEmpty() && discordToken.length() != 59) {
                logger.error("Token supplied is invalid. Please check the token in config.json");
            } else {
                logger.error("No discord token supplied. Please enter a token in config.json");
            }
        } else {
            logger.error("Unable to connect to the database. Bot closing.");
            System.exit(0);
        }
    }

    private static void onShardLogin(DiscordApi api) {
        Logger logger = LogManager.getLogger(Main.class);
        logger.info("Shard " + api.getCurrentShard() + " logged in!");
        //api.updateActivity(ActivityType.LISTENING, ("c.help | Shard " + api.getCurrentShard()));
        api.updateActivity("c.help | Shard " + api.getCurrentShard(), "https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        //logger.info("You can invite me by using the following url: " + api.createBotInvite());

        // Add Watchers
        api.addMessageCreateListener(new CommandWatcher());
        //api.addMessageEditListener(new SnipeWatcher());

        // Log a message, if the bot joined or left a server
        api.addServerJoinListener(event -> logger.info("Joined server " + event.getServer().getName()));
        api.addServerLeaveListener(event -> logger.info("Left server " + event.getServer().getName()));
    }

    private static void setLogProperty() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        String filename = year + "-" + month + "-" + day + "-" + "bot.log";

        System.setProperty("cengbot_log_file", logsLocation + filename);
        logger = LogManager.getLogger(Main.class);
    }
}