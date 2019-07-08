/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.Author;
import org.sqlite.JDBC;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Represents an SQLite file database connection manager
 *
 * @author ramidzkh
 */
@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class SQLiteDatabaseConnectionManager implements Closeable {

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
     * @param file
     *         The file where to create or load the database
     */
    public SQLiteDatabaseConnectionManager(File file) {
        url = "jdbc:sqlite:" + file.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/");
        try {
            con = JDBC.createConnection(url, new Properties());

            // Create it
            con.getMetaData().getURL();
            //Try to construct the database if not there
            innitDB(con);
        }
        catch (NoClassDefFoundError | SQLException e) {
            e.printStackTrace();
            con = null;
        }
    }

    /**
     * Gets the associated connection object
     */
    public Connection getConnection() {
        try {
            return isConnected() ? con : JDBC.createConnection(url, new Properties());
        }
        catch (NoClassDefFoundError | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return Is the connection open
     */
    private boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (isConnected())
                con.close();
        }
        catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * This sets up the database and inserts the tables if they are not there
     *
     * @param connection
     *         the connection to use
     *
     * @author duncte123
     */
    private void innitDB(Connection connection) {
        //Not to self: SQLite doesn't have multi line queries
        try {
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS guildSettings " +
                    '(' +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guildId TEXT NOT NULL," +
                    "logChannelId TEXT NULL," +
                    "welcomeLeaveChannel TEXT NULL," +
                    "prefix VARCHAR(255) NOT NULL DEFAULT 'db!'," +
                    "autoRole VARCHAR(255) NULL," +
                    "enableJoinMessage tinyint(1) NOT NULL DEFAULT '0'," +
                    "enableSwearFilter tinyint(1) NOT NULL DEFAULT '0'," +
                    "autoDeHoist tinyint(1) NOT NULL DEFAULT '0'," +
                    "filterInvites tinyint(1) NOT NULL DEFAULT '0'," +
                    "announceNextTrack tinyint(1) NOT NULL DEFAULT '1'," +
                    "customWelcomeMessage TEXT NOT NULL," +
                    "serverDesc TEXT NULL," +
                    "customLeaveMessage TEXT NOT NULL," +
                    "spamFilterState tinyint(1) NOT NULL DEFAULT '0'," +
                    "kickInsteadState tinyint(1) NOT NULL DEFAULT '0'," +
                    "muteRoleId varchar(255) DEFAULT NULL," +
                    "ratelimits TEXT DEFAULT NULL," +
                    "spam_threshold integer(2) NOT NULL DEFAULT 7," +
                    "leave_timeout tinyint(2) NOT NULL DEFAULT 1" +
                    ");"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS `embedSettings`" +
                    "(guild_id INTEGER(20) PRIMARY KEY," +
                    "embed_color INTEGER(10) NOT NULL DEFAULT 0x0751c6);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS `tags`" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "owner_id VARCHAR(255) NOT NULL," +
                    "name VARCHAR(10) NOT NULL," +
                    "content TEXT NOT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bans" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "modUserId VARCHAR(255) NOT NULL," +
                    "userId VARCHAR(255) NOT NULL," +
                    "Username VARCHAR(255) NOT NULL," +
                    "discriminator VARCHAR(4) NOT NULL," +
                    "ban_date DATETIME NOT NULL," +
                    "unban_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "guildId VARCHAR(255) NOT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS customCommands" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guildId VARCHAR(255) NOT NULL," +
                    "invoke VARCHAR(25) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "autoresponse BOOLEAN NOT NULL DEFAULT FALSE);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS warnings" +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "  `mod_id` varchar(255) NOT NULL," +
                    "  `user_id` varchar(300) NOT NULL," +
                    "  `reason` text NOT NULL," +
                    "  `warn_date` date NOT NULL," +
                    "  `expire_date` date NOT NULL," +
                    "  `guild_id` varchar(266) DEFAULT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS oneGuildPatrons" +
                    "(user_id VARCHAR(255) NOT NULL PRIMARY KEY," +
                    "guild_id VARCHAR(255) NOT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS vcAutoRoles" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guild_id VARCHAR(255) NOT NULL," +
                    "voice_channel_id VARCHAR(255) NOT NULL," +
                    "role_id VARCHAR(255) NOT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS blacklists" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guild_id VARCHAR(255) NOT NULL," +
                    "word VARCHAR(255) NOT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS reminders" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id VARCHAR(255) NOT NULL," +
                    "reminder TEXT NOT NULL," +
                    "remind_date DATETIME NOT NULL," +
                    "channel_id VARCHAR(255) DEFAULT NULL);"
            );

            close();
        }
        catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
