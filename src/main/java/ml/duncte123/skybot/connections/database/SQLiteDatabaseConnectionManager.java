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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Pattern;

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
        url = "jdbc:sqlite:" + file.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/");
        try {
            Class.forName("org.sqlite.JDBC");
            con = JDBC.createConnection(url, new Properties());
            
            // Create it
            con.getMetaData().getURL();
            //Try to construct the database if not there
            innitDB(con);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            con = null;
        }
    }

    /**
     * Gets the associated connection object
     */
    @Override
    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return isConnected() ? con : JDBC.createConnection(url, new Properties());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
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
        return "main"; //SQLlite uses 'main' as name for the database
    }

    /**
     * @return If the connection is available, open or closed
     */
    @Override
    public boolean hasSettings() {
        return con != null;
    }

    /**
     * This sets up the database and inserts the tables if they are not there
     * @param connection the connection to use
     */
    private void innitDB(Connection connection) {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS guildSettings " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guildId TEXT NOT NULL," +
                    "guildName TEXT NOT NULL," +
                    "prefix VARCHAR(255) NOT NULL DEFAULT '/'," +
                    "enableJoinMessage tinyint(1) NOT NULL DEFAULT '0'," +
                    "enableSwearFilter tinyint(1) NOT NULL DEFAULT '0'," +
                    "customWelcomeMessage TEXT NOT NULL);" +

                    "CREATE TABLE IF NOT EXISTS bans" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "modUserId VARCHAR(255) NOT NULL," +
                    "userId VARCHAR(255) NOT NULL," +
                    "Username VARCHAR(255) NOT NULL," +
                    "discriminator VARCHAR(4) NOT NULL," +
                    "ban_date DATETIME NOT NULL," +
                    "unban_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "guildId VARCHAR(255) NOT NULL);" +

                    "CREATE TABLE IF NOT EXISTS tags" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "author VARCHAR(255) NOT NULL," +
                    "authorId VARCHAR(255) NOT NULL," +
                    "tagName VARCHAR(10) NOT NULL," +
                    "tagText TEXT NOT NULL);"
            );
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
