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

/**
 * Use abstraction to easier handle {@link DatabaseConnectionManager} for
 * databases and {@link SQLiteDatabaseConnectionManager} for file databases
 */
public interface DBConnectionManager {

    /**
     * @return The connection to use
     */
    public Connection getConnection();

    /**
     * @return Is the connection opened
     */
    public boolean isConnected();

    /**
     * @return The name of the database
     */
    public String getName();

    /**
     * @return Does the database have settings, default to true
     */
    public default boolean hasSettings() {
        return true;
    }
}
