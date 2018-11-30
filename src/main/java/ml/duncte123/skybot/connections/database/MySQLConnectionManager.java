/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.objects.config.DunctebotConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Represents a server database
 */

@Author(nickname = "ramidzkh", author = "Ramid Khan")
class MySQLConnectionManager implements DBConnectionManager {

    private final String dbHost;
    private final String user;
    private final int port;
    private final String dbName;
    private final String pass;
    private Connection connection;

    MySQLConnectionManager(DunctebotConfig.Sql config) {
        this.dbHost = config.host;
        this.port = config.port;
        this.user = config.username;
        this.pass = config.password;
        this.dbName = config.database;
        try {
            this.connection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", dbHost, port, dbName),
                user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will connect to the database for us and return the connection
     *
     * @return The connection to the database
     */
    public Connection getConnection() {
        if (!isConnected()) {
            try {
                this.connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8", dbHost, port, dbName),
                    user, pass);
            } catch (SQLException e) {
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
            connection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
