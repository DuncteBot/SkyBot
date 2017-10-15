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

import com.google.gson.*;

import java.io.File;

public class Config {

    private final Config parent;
    protected final JsonObject config;

    protected Config(final Config parent, final JsonObject config) {
        this.parent = parent;
        this.config = config;
    }

    /**
     * This will try to get data from the config file
     * @param key the key where the setting is located
     * @return the value of the setting
     */
    public String getString(final String key) {
        return this.getJsonPrimitive(key).getAsString();
    }

    /**
     This will try to get data from the config file
     * @param key the key where the setting is located
     * @param defaultValue If this can't be found we will create the option in the config
     * @return the value of the setting
     */
    public final String getString(final String key, final String defaultValue) {
        if (!this.hasKey(key)) {
            this.put(key, defaultValue);
        }
        try {
            return this.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public int getInt(final String key) throws NumberFormatException {
        try {
            return this.getJsonPrimitive(key).getAsInt();
        }
        catch (final NumberFormatException e) {
            throw e;
        }
    }

    public final int getInt(final String key, final int defaultValue) {
        if (!this.hasKey(key))
            this.put(key, defaultValue);
        return this.getInt(key);
    }

    /**
     * This will load from our config with the key
     * @param key the key to find
     * @return this thing called {@link com.google.gson.JsonPrimitive JsonPrimitive}
     */
    public JsonPrimitive getJsonPrimitive(final String key) {
        try  {
            return this.getJsonElement(key).getAsJsonPrimitive();
        }
        catch (final Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * This will load from our config with the key
     * @param key the key to find
     * @return a nice JsonElement
     * @throws NullPointerException When things are about too go down
     */
    public JsonElement getJsonElement(final String key) throws NullPointerException {
        final String[] path = key.split("\\.");
        JsonElement value = this.config;
        try {
            for (String element : path)  {
               // System.out.println(element);
                if (element.trim().isEmpty())
                    continue;
                if (element.endsWith("]") && element.contains("[")) {
                    final int i = element.lastIndexOf("[");
                    int index;
                    try {
                        index = Integer.parseInt(element.substring(i).replace("[", "").replace("]", ""));
                    }
                    catch (final Exception e)  {
                        index = 0;
                    }
                    element = element.substring(0, i);

                    value = value.getAsJsonObject().get(element);
                    value = value.getAsJsonArray().get(index);

                } else
                    value = value.getAsJsonObject().get(element);
            }
            if (value == null)
                throw new NullPointerException("Key '" + key + "' has no value or doesn't exists, trying to add it");
            return value;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This will check if the key that we are looking for
     * @param key the key to find
     * @return true if the key is there
     */
    public boolean hasKey(final String key)  {
        try {
            return this.getJsonElement(key) != null;
        }
        catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * This will attempt to put a value is the config
     * @param key the key to add the value under
     * @param value the value that we need to add, in the form of an {@link com.google.gson.JsonElement JsonElement}
     * @throws Exception when we fail
     */
    public void put(String key, JsonElement value) throws Exception {
        final String finalKey = key.substring(key.lastIndexOf(".") + 1);
        key = replaceLast(key, finalKey, "");
        if (key.endsWith("."))
            key = replaceLast(key, ".", "");
        final String[] path = key.split("\\.");
        JsonObject current = this.config;

        try {
            for (String element : path)  {
                if (element.trim().isEmpty())
                    continue;
                if (element.endsWith("]") && element.contains("[")) {
                    final int i = element.lastIndexOf("[");
                    int index;
                    try  {
                        index = Integer.parseInt(element.substring(i).replace("[", "").replace("]", ""));
                    }
                    catch (final Exception e)  {
                        index = -1;
                    }
                    element = element.substring(0, i);

                    if (!current.has(element))
                        current.add(element, new JsonArray());
                    final JsonArray array = current.get(element).getAsJsonArray();
                    if (index == -1) {
                        final JsonObject object = new JsonObject();
                        array.add(object);
                        current = object;
                    }
                    else {
                        if (index == array.size())
                            array.add(new JsonObject());
                        current = array.get(index).getAsJsonObject();
                    }

                } else {
                    if (!current.has(element))
                        current.add(element, new JsonObject());
                    current = current.get(element).getAsJsonObject();
                }
            }
        }
        catch (final Exception e) {
            e.printStackTrace();
            throw e;
        }
        current.add(finalKey, value);
        this.save();
    }

    //public void put(String key, JsonElement value) { System.out.println("Key: " + key+" val: "+value); }

    /**
     * This will attempt to put a value is the config
     * @param key the key to add the value under
     * @param value the value that we need to add
     */
    public void put(final String key, final String value) {
        try {
            this.put(key, new JsonPrimitive(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will attempt to put a value is the config
     * @param key the key to add the value under
     * @param value the value that we need to add
     */
    public void put(final String key, final Number value) {
        try {
            this.put(key, new JsonPrimitive(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * idk
     * @param text the text to replace
     * @param regex the regex or something
     * @param replacement what to replace it with
     * @return the replaced string
     */
    public static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    /**
     * get the config as a file
     * @return the config as a file
     */
    public File getConfigFile() {
        return this.parent.getConfigFile();
    }

    /**
     * save the config
     * @throws Exception when things break
     */
    public void save() throws Exception {
        this.parent.save();
    }
}
