package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.config.Config;
import ml.duncte123.skybot.config.ConfigLoader;
import org.slf4j.event.Level;

import java.io.File;

public class ConfigUtils {

    private static Config config;

    public ConfigUtils() {
        try {

            config = ConfigLoader.getConfig(new File("config.json"));
        } catch (Exception e) {
            AirUtils.log(Level.ERROR, "Could not load config, aborting");
            System.exit(-1);
        }
    }

    public Config getConfig() {
        return config;
    }
}
