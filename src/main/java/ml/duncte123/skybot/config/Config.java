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

package ml.duncte123.skybot.config;

import ml.duncte123.skybot.utils.AirUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.event.Level;

import java.io.File;

public class Config {
    
    protected JSONObject config;
    private final Config parent;
    
    protected Config(Config parent, JSONObject config) {
        this.parent = parent;
        this.config = config;
    }
    
    /**
     * idk
     *
     * @param text        the text to replace
     * @param regex       the regex or something
     * @param replacement what to replace it with
     * @return the replaced string
     */
    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }
    
    /**
     * This will try to get data from the config file
     *
     * @param key the key where the setting is located
     * @return the value of the setting
     */
    public String getString(String key) {
        Object out = this.get(this.config, key);
        return (String) out;
    }
    
    /**
     * This will try to get data from the config file
     *
     * @param key          the key where the setting is located
     * @param defaultValue If this can't be found we will create the option in the config
     * @return the value of the setting
     */
    public final String getString(String key, String defaultValue) {
        Object out = this.get(this.config, key);
        return (out == null) ? defaultValue : (String) out;
    }
    
    /**
     * This will attempt to get an integer from the config file
     *
     * @param key The key to get the int from
     * @return the int
     * @throws NumberFormatException if the returned value isn't valis
     */
    public int getInt(String key) throws NumberFormatException {
        Object out = this.get(this.config, key);
        return (int) out;
    }
    
    /**
     * This will attempt to get an integer from the config file
     *
     * @param key          The key to get the int from
     * @param defaultValue the value to put it on if the int can't be found
     * @return the int
     */
    public final int getInt(String key, int defaultValue) {
        Object out = this.get(this.config, key);
        return (out == null) ? defaultValue : (int) out;
    }
    
    /**
     * This will attempt to get a boolean from the config file
     *
     * @param key the key to get the boolean from
     * @return the boolean from the key
     */
    public boolean getBoolean(String key) {
        Object out = this.get(this.config, key);
        return (boolean) out;
    }
    
    /**
     * This will attempt to get a boolean from the config file
     *
     * @param key          the key to get the boolean from
     * @param defaultValue the default value to put the boolean on when it can't be found
     * @return the boolean from the key
     */
    public final boolean getBoolean(String key, boolean defaultValue) {

        Object out = this.get(this.config, key);
        return (out == null) ? defaultValue : (boolean) out;
    }

    public void put(String key, Object value) {
        final String[] path = key.split("\\.");

        try {
            JSONObject temp = this.config;
            for (int i = 0; i < path.length; i++) {
                if (i == path.length - 1) {
                    temp.put(path[i], value);
                    break;
                }
                Object x = this.get(temp, path[i]);
                if (x == null) {
                    if (path.length == 1) {
                        this.config.put(key, value);
                    } else {
                        AirUtils.log(Level.ERROR, "Lol is this possible?");
                    }
                } else {
                    temp = new JSONObject(x.toString());
                    temp = new JSONObject().put(path[i], temp);
                }
            }
            this.config = temp;
        } catch (JSONException ex) {
            AirUtils.log(Level.ERROR, ex.toString());
        }

        try {
            this.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This methods gets an item of an JSON really quick but
     * can't be used for putting items!
     * @param jsonData the json where it should search
     * @param key key aka path in the json
     * @return Object probably a json or any other..
     */
    private Object get(JSONObject jsonData, String key) {
        if (key.isEmpty())
            return null;
        final String[] path = key.split("\\.");
        JSONObject current = jsonData;
            for (int i = 0; i < path.length; i++) {
                try {
                    if (jsonData.isNull(path[i]))
                        jsonData.put(path[i], new Object());
                    if (i == path.length - 1)
                        return current.get(path[i]);
                    current = new JSONObject(current.get(path[i]).toString());
                } catch (JSONException ex) {
                    AirUtils.log(Level.ERROR, ex.toString());
                }
            }
        return null;
    }
    
    /**
     * get the config as a file
     *
     * @return the config as a file
     */
    public File getConfigFile() {
        return this.parent.getConfigFile();
    }
    
    /**
     * save the config
     *
     * @throws Exception when things break
     */
    public void save() throws Exception {
        this.parent.save();
    }
}
