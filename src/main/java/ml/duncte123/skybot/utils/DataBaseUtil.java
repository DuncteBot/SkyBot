package ml.duncte123.skybot.utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseUtil {

    /**
     * This will connect to the database for us and return the connection
     * @return The connection to the database
     */
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

    /**
     * This will give the database name that we specified in the config
     * @return the database name
     */
    public static String getDbName() {
        return ResourceUtil.getDBProperty("dbname");
    }

    /**
     * This will check if the database is connected
     * @return true if we are connected
     */
    public static boolean checkDbConn() {
        return getConnection() != null;
    }
}
