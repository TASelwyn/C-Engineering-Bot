package wtf.devil.cengbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.DevilsBot;
import wtf.devil.cengbot.utils.objects.BotConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static wtf.devil.cengbot.Constants.*;

public class Config {

    Logger logger = LogManager.getLogger(DevilsBot.class);

    public boolean doesConfigExist() {
        File data = new File(configLocation);
        return data.exists();
    }

    public String getToken() {
        return getConfig().getToken();
    }

    public BotConfig getConfig() {
        if (doesConfigExist()) {
            try {
                Gson gson = new Gson();

                logger.info("Reading config...");

                Reader reader = Files.newBufferedReader(Paths.get(configLocation));
                BotConfig config = gson.fromJson(reader, BotConfig.class);
                reader.close();

                if (config == null || !config.isValid()) {
                    logger.warn("Configuration invalid, please fix values");
                    return config;
                }
                logger.info("Config read successfully");
                return config;

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("Failed to read config");
            createConfig();
            return getConfig();
        }

        return null;
    }

    public void createConfig() {
        logger.info("Creating new config...");

        try {
            GsonBuilder gson = new GsonBuilder();
            gson.setPrettyPrinting();

            FileWriter dataWriter = new FileWriter(configLocation);

            BotConfig config = new BotConfig();
            gson.create().toJson(config, dataWriter);

            dataWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
