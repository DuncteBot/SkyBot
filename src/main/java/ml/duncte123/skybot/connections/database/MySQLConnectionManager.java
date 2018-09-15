/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.mysql.cj.exceptions.InvalidConnectionAttributeException;
import ml.duncte123.skybot.objects.config.DunctebotConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a server database
 */
@SuppressWarnings("SqlDialectInspection")
class MySQLConnectionManager implements DBConnectionManager {

    private final String dbHost;
    private final String user;
    private final int port;
    private final String dbName;
    private final String timezone;
    private final String pass;
    private Connection connection;

    MySQLConnectionManager(DunctebotConfig.Sql config) {
        this.dbHost = config.host;
        this.port = config.port;
        this.user = config.username;
        this.pass = config.password;
        this.dbName = config.database;
        this.timezone = config.timezone;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", dbHost, port, dbName),
                    user, pass);
        } catch (SQLException | InvalidConnectionAttributeException e) {
            // Try again if the time zones conflict with the DB like "Mitteleuropäische Sommerzeit"
            try {
                this.connection = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=%s", dbHost, port, dbName, timezone),
                        user, pass);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        innitDB(getConnection());
    }

    /**
     * This will connect to the database for us and return the connection
     *
     * @return The connection to the database
     */
    public Connection getConnection() {
        if (!isConnected()) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                this.connection = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", dbHost, port, dbName),
                        user, pass);
            } catch (SQLException | InvalidConnectionAttributeException e) {
                // Try again if the time zones conflict with the DB like "Mitteleuropäische Sommerzeit"
                try {
                    this.connection = DriverManager.getConnection(
                            String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=%s", dbHost, port, dbName, timezone),
                            user, pass);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return connection;
    }

    /**
     * This will check if we have some settings in the databse
     *
     * @return true if every sql field is set
     */
    @Override
    public boolean hasSettings() {
        try {
            return !dbHost.isEmpty() && !user.isEmpty() && !dbName.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This will check if the database is connected
     *
     * @return true if we are connected
     */
    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This will give the database name that we specified in the config
     *
     * @return the database name
     */
    @Override
    public String getName() {
        return dbName;
    }

    @Override
    public void close() throws IOException {
        try {
            if (isConnected())
                connection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private void innitDB(Connection connection) {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `bans` (\n" +
                    "`id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `modUserId` varchar(255) NOT NULL,\n" +
                    "  `userId` varchar(300) NOT NULL,\n" +
                    "  `Username` varchar(266) NOT NULL,\n" +
                    "  `discriminator` varchar(4) NOT NULL,\n" +
                    "  `ban_date` datetime NOT NULL,\n" +
                    "  `unban_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                    "  `guildId` varchar(266) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `footerQuotes` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` varchar(200) NOT NULL COMMENT 'Username',\n" +
                    "  `quote` text NOT NULL COMMENT 'Quote',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `tags` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `author` varchar(255) NOT NULL,\n" +
                    "  `authorId` varchar(255) NOT NULL,\n" +
                    "  `tagName` varchar(10) NOT NULL,\n" +
                    "  `tagText` text NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `guildSettings` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `guildId` text NOT NULL,\n" +
                    "  `guildName` text CHARACTER SET utf8mb4,\n" +
                    "  `prefix` varchar(255) NOT NULL DEFAULT '/',\n" +
                    "  `autoRole` varchar(255) DEFAULT NULL,\n" +
                    "  `enableJoinMessage` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `enableSwearFilter` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `autoDeHoist` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `filterInvites` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `announceNextTrack` tinyint(1) NOT NULL DEFAULT '1',\n" +
                    "  `customWelcomeMessage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,\n" +
                    "  `customLeaveMessage` text DEFAULT NULL,\n" +
                    "  `serverDesc` text DEFAULT NULL,\n" +
                    "  `logChannelId` varchar(255) DEFAULT NULL,\n" +
                    "  `welcomeLeaveChannel` varchar(255) DEFAULT NULL,\n" +
                    "  `spamFilterState` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `kickInsteadState` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `muteRoleId` varchar(255) DEFAULT NULL,\n" +
                    "  `ratelimits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci DEFAULT NULL,\n" +
                    "PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci;");

            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS customCommands\n" +
                            "(id int(11) AUTO_INCREMENT,\n" +
                            "guildId VARCHAR(255) NOT NULL,\n" +
                            "invoke VARCHAR(10) NOT NULL,\n" +
                            "message TEXT NOT NULL,\n" +
                            "PRIMARY KEY (`id`));"
            );

            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS animal_apis\n" +
                            "(`id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `file` text NOT NULL,\n" +
                            "  `api` varchar(255) NOT NULL,\n" +
                            "  PRIMARY KEY (`id`));"
            );
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS warnings\n" +
                            "(`id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `mod_id` varchar(255) NOT NULL,\n" +
                            "  `user_id` varchar(300) NOT NULL,\n" +
                            "  `reason` text NOT NULL,\n" +
                            "  `warn_date` date NOT NULL,\n" +
                            "  `expire_date` date NOT NULL,\n" +
                            "  `guild_id` varchar(266) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`));"
            );
            close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}