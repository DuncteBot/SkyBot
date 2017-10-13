package ml.duncte123.skybot.connections.database;

import java.sql.Connection;

public class DBManager {

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

    public final DatabaseConnectionManager connManager;

    /**
     * This will set our stuff up
     */
    public DBManager() {
        this.connManager = new DatabaseConnectionManager();
        this.isConnected = connManager.checkDbConn();
        this.name = connManager.getDbName();
        this.connection = connManager.getConnection();
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
     *
     * @deprecated use {@link #getConnManager()} instead
     */
    @Deprecated
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Returns the connection manager
     * @return the {@link ml.duncte123.skybot.connections.database.DatabaseConnectionManager DatabaseConnectionManager}
     */
    public DatabaseConnectionManager getConnManager() {
        return connManager;
    }
}
