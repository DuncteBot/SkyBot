/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import java.io.Closeable;
import java.sql.Connection;

/**
 * Use abstraction to easier handle {@link MySQLConnectionManager} for
 * databases and {@link SQLiteDatabaseConnectionManager} for file databases
 *
 * @author ramidzkh
 */
public interface DBConnectionManager
extends Closeable {

    /**
     * @return The connection to use
     */
    Connection getConnection();

    /**
     * @return Is the connection opened
     */
    boolean isConnected();

    /**
     * @return The name of the database
     */
    String getName();

    /**
     * @return Does the database have settings, default to true
     */
    default boolean hasSettings() {
        return true;
    }
}
