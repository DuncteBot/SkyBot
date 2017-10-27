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

class SQLiteDatabaseConnectionManager
implements DBConnectionManager {

    private final String url;
    private Connection con;

    SQLiteDatabaseConnectionManager(File file) {
        url = "jdbc:sqlite:" + file.getAbsolutePath();
        try {
            con = JDBC.createConnection(url, new Properties());
            
            con.getMetaData().getURL();
        } catch (SQLException e) {
            e.printStackTrace();
            con = null;
        }
    }

    @Override
    public Connection getConnection() {
        return con;
    }

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

    @Override
    public String getName() {
        return url;
    }

    @Override
    public boolean hasSettings() {
        return con != null;
    }
}
