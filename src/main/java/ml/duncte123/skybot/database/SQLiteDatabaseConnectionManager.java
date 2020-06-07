/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.database;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.api.Patron;
import org.sqlite.JDBC;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class SQLiteDatabaseConnectionManager {
    private final String url;
    private Connection con;

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

    public Connection getConnection() {
        try {
            return isConnected() ? con : JDBC.createConnection(url, new Properties());
        }
        catch (NoClassDefFoundError | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isConnected() {
        try {
            return con != null && !con.isClosed();
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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
                    "enableJoinMessage TINYINT(1) NOT NULL DEFAULT '0'," +
                    "enableSwearFilter TINYINT(1) NOT NULL DEFAULT '0'," +
                    "autoDeHoist TINYINT(1) NOT NULL DEFAULT '0'," +
                    "filterInvites TINYINT(1) NOT NULL DEFAULT '0'," +
                    "announceNextTrack TINYINT(1) NOT NULL DEFAULT '1'," +
                    "customWelcomeMessage TEXT NOT NULL," +
                    "serverDesc TEXT NULL," +
                    "customLeaveMessage TEXT NOT NULL," +
                    "spamFilterState TINYINT(1) NOT NULL DEFAULT '0'," +
                    "kickInsteadState TINYINT(1) NOT NULL DEFAULT '0'," +
                    "muteRoleId VARCHAR(255) DEFAULT NULL," +
                    "ratelimits TEXT DEFAULT NULL," +
                    "spam_threshold integer(2) NOT NULL DEFAULT 7," +
                    "leave_timeout TINYINT(2) NOT NULL DEFAULT 1," +
                    "logBan TINYINT(1) NOT NULL DEFAULT '1'," +
                    "logUnban TINYINT(1) NOT NULL DEFAULT '1'," +
                    "logKick TINYINT(1) NOT NULL DEFAULT '1'," +
                    "logMute TINYINT(1) NOT NULL DEFAULT '1'," +
                    "logWarn TINYINT(1) NOT NULL DEFAULT '1'," +
                    "profanity_type VARCHAR(20) default 'SEVERE_TOXICITY'," +
                    "aiSensitivity FLOAT(3, 2) default 0.7," +
                    "allow_all_to_stop TINYINT(1) NOT NULL DEFAULT '1'" +
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
                    "  `mod_id` VARCHAR(255) NOT NULL," +
                    "  `user_id` VARCHAR(300) NOT NULL," +
                    "  `reason` TEXT NOT NULL," +
                    "  `warn_date` DATE NOT NULL," +
                    "  `expire_date` DATE NOT NULL," +
                    "  `guild_id` VARCHAR(266) DEFAULT NULL);"
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
                    "remind_create_date DATETIME NOT NULL," +
                    "remind_date DATETIME NOT NULL," +
                    "channel_id VARCHAR(255) DEFAULT NULL);"
            );

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS mutes" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "guild_id VARCHAR(255) NOT NULL," +
                    "mod_id VARCHAR(255) NOT NULL," +
                    "user_id VARCHAR(255) NOT NULL," +
                    "user_tag VARCHAR(255) NOT NULL," +
                    "mute_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "unmute_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ");"
            );

            final String namesList = Arrays.stream(Patron.Type.values())
                .map(Patron.Type::name)
                .collect(Collectors.joining("', '"));

            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS patrons(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id VARCHAR(20) NOT NULL," +
                    "guild_id VARCHAR(20)," +
                    "type TEXT CHECK( type IN ('" + namesList + "')) NOT NULL" +
                    ");"
            );
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
