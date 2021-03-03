package wtf.devil.cengbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.Main;
import wtf.devil.cengbot.utils.objects.configObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static wtf.devil.cengbot.Constants.*;

public class Config {

    Logger logger = LogManager.getLogger(Main.class);

    public boolean doesConfigExist() {
        File data = new File(configLocation);

        return data.exists();
    }

    public String getToken() {
        return getConfig().getToken();
    }

    public configObject getConfig() {
        if (doesConfigExist()) {
            try {
                Gson gson = new Gson();

                logger.info("Reading config...");

                Reader reader = Files.newBufferedReader(Paths.get(configLocation));
                configObject object = gson.fromJson(reader, configObject.class);

                reader.close();

                if (object != null) {
                    logger.info("Config read successfully");
                } else {
                    logger.info("Config is empty....");
                    createConfig();
                    return getConfig();
                }
                return object;

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.info("No config exists...");
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

            configObject config = new configObject("");

            gson.create().toJson(config, dataWriter);

            dataWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
