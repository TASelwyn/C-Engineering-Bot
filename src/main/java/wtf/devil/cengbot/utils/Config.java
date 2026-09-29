package wtf.devil.cengbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.nio.file.StandardCopyOption;

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
                JsonElement json = JsonParser.parseReader(reader);
                reader.close();

                BotConfig config = gson.fromJson(json, BotConfig.class);

                // Write back any fields missing from the file so new options show up with their defaults
                if (config != null && json.isJsonObject() && hasMissingFields(json.getAsJsonObject(), gson.toJsonTree(config).getAsJsonObject())) {
                    logger.info("Config is missing fields, adding defaults (old config saved to {}.bak)...", configLocation);
                    // Copy rather than move, a bind-mounted config.json can't be renamed inside Docker
                    Files.copy(Paths.get(configLocation), Paths.get(configLocation + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                    saveConfig(config);
                }

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

    private boolean hasMissingFields(JsonObject file, JsonObject expected) {
        for (String key : expected.keySet()) {
            if (!file.has(key)) {
                return true;
            }
        }
        return false;
    }

    public void createConfig() {
        logger.info("Creating new config...");
        saveConfig(new BotConfig());
    }

    public void saveConfig(BotConfig config) {
        try {
            GsonBuilder gson = new GsonBuilder();
            gson.setPrettyPrinting();

            Files.createDirectories(Paths.get(configLocation).getParent());
            FileWriter dataWriter = new FileWriter(configLocation);

            gson.create().toJson(config, dataWriter);

            dataWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
