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
