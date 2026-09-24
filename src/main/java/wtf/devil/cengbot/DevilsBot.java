package wtf.devil.cengbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.utils.Config;
import wtf.devil.cengbot.utils.objects.BotConfig;
import wtf.devil.cengbot.utils.watchers.BotLifecycleListener;
import wtf.devil.cengbot.utils.watchers.CommandWatcher;

import java.util.Calendar;
import java.util.EnumSet;
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

        try {
            JDABuilder.create(discordToken, EnumSet.allOf(GatewayIntent.class))
                    .enableCache(CacheFlag.EMOJI)
                    .setActivity(Activity.streaming("c.help", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
                    .addEventListeners(new CommandWatcher(), new BotLifecycleListener())
                    .build();
        } catch (InvalidTokenException e) {
            logger.error("Token supplied is invalid. Please enter a valid token in config.json");
            System.exit(0);
        }

        logger.info("Bot successfully started.");
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
