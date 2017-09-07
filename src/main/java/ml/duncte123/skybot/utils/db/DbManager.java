package ml.duncte123.skybot.utils.db;

import java.sql.Connection;

public class DbManager {

    /**
     * This is the database name
     */
    private final String name;
    /**
     * This is true if we are connected to the database
     */
    private final boolean isConnected;
    /**
     * This will hold our connection
     */
    private final Connection connection;

    /**
     * This will set our stuff up
     */
    public DbManager() {
        this.name = DataBaseUtil.getDbName();
        this.isConnected = DataBaseUtil.checkDbConn();
        this.connection = DataBaseUtil.getConnection();
    }

    /**
     * This will check the connection for us
     * @return true if we are connected
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * This will return the name of the connected database
     * @return The name of the connected database
     */
    public String getName() {
        return this.name;
    }

    /**
     * This will get the connection for us
     * @return the connection, will we null if we aren't connected
     */
    public Connection getConnection() {
        return this.connection;
    }
}
