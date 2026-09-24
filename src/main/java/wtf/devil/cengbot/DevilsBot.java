package wtf.devil.cengbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.util.logging.ExceptionLogger;
import wtf.devil.cengbot.utils.Config;
import wtf.devil.cengbot.utils.DiscordShardRegistry;
import wtf.devil.cengbot.utils.objects.BotConfig;
import wtf.devil.cengbot.utils.watchers.CommandWatcher;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static wtf.devil.cengbot.Constants.*;
import static wtf.devil.cengbot.utils.database.DatabaseManager.databaseTestConnection;

public class DevilsBot {

    private static BotConfig config;
    private static Logger logger;

    private static final ExecutorService redisCallbackPool = Executors.newSingleThreadExecutor();
    //private final Wooclapper wooclapper = new Wooclapper();

    static void main(String[] args) {
        setLogProperty();
        logger.info("Devils Engineering Bot");
        config = new Config().getConfig();
        if (!config.isValid()) {
            logger.error("The configuration provided is NOT valid. Exiting.");
            System.exit(0);
        }

        if (!databaseTestConnection()) {
            logger.error("Unable to connect to the database. Bot closing.");
            System.exit(0);
        }

        String discordToken = config.getToken();

        //setupRedis();

        if (discordToken.length() != 70) {
            logger.error("Token supplied is invalid. Please enter a token in config.json");
        }

        new DiscordApiBuilder()
                .setToken(discordToken)
                .setAllIntents()
                .setTotalShards(1)
                //.setRecommendedTotalShards().join()
                .loginAllShards()
                .forEach(shardFuture -> shardFuture
                        .thenAcceptAsync(DevilsBot::onShardLogin)
                        .exceptionally(ExceptionLogger.get())
                );

        logger.info("Bot successfully started.");
    }

    private static void onShardLogin(DiscordApi api) {
        Logger logger = LogManager.getLogger(DevilsBot.class);
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

        DiscordShardRegistry.register(api);

        logger.info("Shard {} ready ", api.getCurrentShard());
    }

    private static void setLogProperty() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        String filename = year + "-" + month + "-" + day + "-" + "bot.log";

        System.setProperty("cengbot_log_file", logsLocation + filename);
        logger = LogManager.getLogger(DevilsBot.class);
    }

//    private static void setupRedis() {
//        try {
//            redisModule = new RedisModule(config.getRedisUri());
//            redisModulePub = new RedisModulePub(redisModule, config.getRedisPrefix());
//            redisModuleSub = new RedisModuleSub(redisModule, redisCallbackPool, ((channel, message) -> {
//                logger.info("Redis msg {}: {}", channel, message);
//
//                if (!Objects.equals(channel, "orchestrator:status")) {
//                    DiscordShardRegistry.getApi().getTextChannelById(1377083789892780043L).ifPresent(tc -> tc.sendMessage(message));
//                }
//
//            }));
//
//            redisModuleSub.subscribe(List.of("bot:commands", "bot:events", "orchestrator:status"));
//
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        }
//    }
}