package ml.duncte123.skybot.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseUtil {

    public static Connection getConnection() {
        try {
            String dbHost = ResourceUtil.getDBProperty("host");
            String user = ResourceUtil.getDBProperty("username");
            String pass = ResourceUtil.getDBProperty("password");
            String dbName = ResourceUtil.getDBProperty("dbname");
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
        return ResourceUtil.getDBProperty("dbname");
    }
}
