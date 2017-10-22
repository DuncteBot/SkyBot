/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
