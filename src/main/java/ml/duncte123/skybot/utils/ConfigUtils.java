/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
     *
     * @return the config for the bot
     */
    public Config loadConfig() {
        return config;
    }
}
