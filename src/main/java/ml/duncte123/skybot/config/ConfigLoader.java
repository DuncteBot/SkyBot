/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
 */

package ml.duncte123.skybot.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.text.translate.UnicodeUnescaper;

import java.io.*;

public class ConfigLoader {

    /**
     * This will attempt to load the config and create it if it is not there
     *
     * @param file the file to load
     * @return the loaded config
     * @throws Exception if something goes wrong
     */
    public static Config getConfig(final File file) throws Exception {
        if (!file.exists()) {
            file.createNewFile();
            final FileWriter writer = new FileWriter(file);
            writer.write("{}");
            writer.close();
        }
        return new MainConfig(file);
    }

    public static class MainConfig extends Config {

        private final File configFile;

        MainConfig(final File file) throws Exception {
            super(null, new JsonParser().parse(new FileReader(file)).getAsJsonObject());
            this.configFile = file;
        }

        @Override
        public File getConfigFile() {
            return this.configFile;
        }

        @Override
        public void save() throws Exception {
            final Gson gson = new GsonBuilder()
                                      .serializeNulls()
                                      .setPrettyPrinting()
                                      .serializeSpecialFloatingPointValues()
                                      .create();
            final String json = gson.toJson(this.config);
            try {
                final BufferedWriter writer = new BufferedWriter(
                                                                        new OutputStreamWriter(new FileOutputStream(this.configFile), "UTF-8"));
                new UnicodeUnescaper().translate(json, writer);
                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

    }
}
