/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
     * @return the config for the bot
     */
    public Config loadConfig() {
        return config;
    }
}
