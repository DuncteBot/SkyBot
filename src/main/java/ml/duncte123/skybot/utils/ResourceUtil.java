package ml.duncte123.skybot.utils;

import java.util.ResourceBundle;

public class ResourceUtil {

    public static String getDBProperty(String key) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("sql");

        return resourceBundle.getString(key);
    }
}
