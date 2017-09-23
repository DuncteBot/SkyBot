package ml.duncte123.skybot.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Config {

    private final Config parent;
    protected boolean autoSave;
    protected final JsonObject config;

    protected Config(final Config parent, final JsonObject config) {
        this.parent = parent;
        this.config = config;
    }

    public String getString(final String key) {
        return this.getJsonPrimitive(key).getAsString();
    }

    public final String getString(final String key, final String defaultValue) {
        if (!this.hasKey(key))
            this.put(key, defaultValue);
        try {
            return this.getString(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public JsonPrimitive getJsonPrimitive(final String key) {
        try  {
            return this.getJsonElement(key).getAsJsonPrimitive();
        }
        catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonElement getJsonElement(final String key) throws Exception  {
        final String[] path = key.split("\\.");
        JsonElement value = this.config;
        try {
            for (String element : path)  {
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
                throw new NullPointerException();
            return value;
        }
        catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasKey(final String key)  {
        try {
            this.getJsonElement(key);
        }
        catch (final Exception e) {
            return false;
        }
        return true;
    }

    public void put(String key, final JsonElement value) throws Exception {
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
            throw e;
        }
        current.add(finalKey, value);
    }

    public void put(final String key, final String value) {
        try {
            this.put(key, new JsonPrimitive(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public File getConfigFile() {
        return this.parent.getConfigFile();
    }

    public void save() throws Exception {
        this.parent.save();
    }
}
