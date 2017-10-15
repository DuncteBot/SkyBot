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
