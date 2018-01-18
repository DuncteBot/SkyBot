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

import ml.duncte123.skybot.utils.AirUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a server database
 */
class MySQLConnectionManager
implements DBConnectionManager {

    private Connection connection;

    private final String dbHost;
    private final int port;
    private final String user;
    private final String pass;
    private final String dbName;

    public MySQLConnectionManager() {
        this.dbHost = AirUtils.config.getString("sql.host", "sql.example.com");
        this.port = AirUtils.config.getInt("sql.port", 3306);
        this.user = AirUtils.config.getString("sql.username", "exampleUser");
        this.pass = AirUtils.config.getString("sql.password", "Ex@mplePAss");
        this.dbName = AirUtils.config.getString("sql.database", "Example_database");

        innitDB(getConnection());
    }

    /**
     * This will connect to the database for us and return the connection
     *
     * @return The connection to the database
     */
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", dbHost, port, dbName),
                    user, pass);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            return null;
        }
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
            return getConnection() != null && !connection.isClosed();
        } catch (SQLException e) {
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
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `modUserId` varchar(255) NOT NULL,\n" +
                    "  `userId` varchar(300) NOT NULL,\n" +
                    "  `Username` varchar(266) NOT NULL,\n" +
                    "  `discriminator` varchar(4) NOT NULL,\n" +
                    "  `ban_date` datetime NOT NULL,\n" +
                    "  `unban_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                    "  `guildId` varchar(266) NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `footerQuotes` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `name` varchar(200) NOT NULL COMMENT 'Username',\n" +
                    "  `quote` text NOT NULL COMMENT 'Quote',\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `tags` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `author` varchar(255) NOT NULL,\n" +
                    "  `authorId` varchar(255) NOT NULL,\n" +
                    "  `tagName` varchar(10) NOT NULL,\n" +
                    "  `tagText` text NOT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `guildSettings` (\n" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                    "  `guildId` text NOT NULL,\n" +
                    "  `guildName` text CHARACTER SET utf8mb4,\n" +
                    "  `prefix` varchar(255) NOT NULL DEFAULT '/',\n" +
                    "  `autoRole` varchar(255) DEFAULT NULL,\n" +
                    "  `enableJoinMessage` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `enableSwearFilter` tinyint(1) NOT NULL DEFAULT '0',\n" +
                    "  `customWelcomeMessage` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci NOT NULL,\n" +
                    "  `customLeaveMessage` text NOT NULL,\n" +
                    "  `logChannelId` varchar(255) DEFAULT NULL,\n" +
                    "  `welcomeLeaveChannel` varchar(255) DEFAULT NULL,\n" +
                    "PRIMARY KEY (`id`)\n" +
                    ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
            if(connection.createStatement().executeQuery("SELECT COUNT(id) FROM footerQuotes").getFetchSize() == 0) {
                connection.createStatement().executeUpdate("INSERT INTO footerQuotes " +
                        "VALUES (DEFAULT, 'duncte123', 'FIRST')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}