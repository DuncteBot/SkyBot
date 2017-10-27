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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.sqlite.JDBC;

/**
 * Represents an SQLite file database {@link DBConnectionManager connection manager}
 * 
 * @author ramidzkh
 *
 */
class SQLiteDatabaseConnectionManager
implements DBConnectionManager {

    /**
     * The URL of this database
     */
    private final String url;

    /**
     * The associated connection object
     */
    private Connection con;

    /**
     * Constructs a new SQLite file database
     * 
     * @param file The file where to create or load the database
     */
    SQLiteDatabaseConnectionManager(File file) {
        url = "jdbc:sqlite:" + file.getAbsolutePath();
        try {
            con = JDBC.createConnection(url, new Properties());
            
            // Create it
            con.getMetaData().getURL();
        } catch (SQLException e) {
            e.printStackTrace();
            con = null;
        }
    }

    /**
     * Gets the associated connection object
     */
    @Override
    public Connection getConnection() {
        return con;
    }

    /**
     * @return Is the connection open
     */
    @Override
    public boolean isConnected() {
        if(con == null)
            return false;
        try {
            return !con.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return The URL of this database
     */
    @Override
    public String getName() {
        return url;
    }

    /**
     * @return If the connection is available, open or closed
     */
    @Override
    public boolean hasSettings() {
        return con != null;
    }
}
