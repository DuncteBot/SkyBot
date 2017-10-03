package ml.duncte123.skybot.connections.database;

import ml.duncte123.skybot.utils.AirUtils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionManager {

    private final String dbHost;
    private final String user;
    private final String pass;
    private final String dbName;

    public DatabaseConnectionManager() {
        this.dbHost = AirUtils.config.getString("sql.host", "sql.example.com");
        this.user = AirUtils.config.getString("sql.username", "exampleUser");
        this.pass = AirUtils.config.getString("sql.password", "Ex@mplePAss");
        this.dbName = AirUtils.config.getString("sql.database", "Example_database");
    }

    /**
     * This will connect to the database for us and return the connection
     * @return The connection to the database
     */
    public Connection getConnection() {
        /*String dbHost = AirUtils.config.getString("sql.host", "host");
        String user = AirUtils.config.getString("sql.username", "usn");
        String pass = AirUtils.config.getString("sql.password", "pass");
        String dbName = AirUtils.config.getString("sql.database", "db");*/
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://"+ dbHost +"/"+ dbName, user , pass);
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * This will give the database name that we specified in the config
     * @return the database name
     */
    public String getDbName() {
        //return AirUtils.config.getString("sql.database", "db");
        return dbName;
    }

    /**
     * This will check if the database is connected
     * @return true if we are connected
     */
    public boolean checkDbConn() {
        return getConnection() != null;
    }

    /**
     * This will check if we have some settings in the databse
     * @return true if every sql field is set
     */
    public boolean hasSettings() {
        /*String dbHost = AirUtils.config.getString("sql.host", "host");
        String user = AirUtils.config.getString("sql.username", "usn");
        String pass = AirUtils.config.getString("sql.password", "pass");
        String dbName = AirUtils.config.getString("sql.database", "db");*/
        try {
            return !dbHost.isEmpty() && !user.isEmpty() && !pass.isEmpty() && !dbName.isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }
}