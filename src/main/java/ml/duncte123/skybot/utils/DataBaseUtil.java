package ml.duncte123.skybot.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseUtil {

    public static Connection getConnection() {
        try {
            String dbHost = DbSettings.getProperty("host");
            String user = DbSettings.getProperty("username");
            String pass = DbSettings.getProperty("password");
            String dbName = DbSettings.getProperty("dbname");
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager
                    .getConnection("jdbc:mysql://"+ dbHost +"/"+ dbName +"?"
                            + "user="+ user +"&password=" + pass);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static String getDbName() {
        return DbSettings.getProperty("dbname");
    }
}
