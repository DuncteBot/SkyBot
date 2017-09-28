package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.config.Config;
import ml.duncte123.skybot.config.ConfigLoader;
import org.slf4j.event.Level;

import java.io.File;

public class ConfigUtils {

    private Config config;

    /**
     * This will try to load the bot config and kill the program if it fails
     */
    public ConfigUtils() {
        try {
            AirUtils.log(Level.INFO, "Loading config.json");
            this.config = ConfigLoader.getConfig(new File("config.json"));
            AirUtils.log(Level.INFO, "Loaded config.json");
        } catch (Exception e) {
            AirUtils.log(Level.ERROR, "Could not load config, aborting");
            System.exit(-1);
        }
    }

    /**
     * This will return the config that we have
     * @return the config for the bot
     */
    public Config loadConfig() {
        return config;
    }
}
