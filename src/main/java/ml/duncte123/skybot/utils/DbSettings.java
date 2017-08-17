package ml.duncte123.skybot.utils;

import java.util.ResourceBundle;

public class DbSettings {

    public static String getProperty(String key) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("sql");

        return resourceBundle.getString(key);
    }
}
