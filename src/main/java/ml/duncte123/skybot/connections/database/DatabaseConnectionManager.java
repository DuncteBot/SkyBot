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

import ml.duncte123.skybot.utils.AirUtils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionManager {

    private final String dbHost;
    private final String user;
    private final String pass;
    private final String dbName;

    public DatabaseConnectionManager() {
        this.dbHost = AirUtils.config.getString("sql.host", "sql.example.com");
        this.user = AirUtils.config.getString("sql.username", "exampleUser");
        this.pass = AirUtils.config.getString("sql.password", "Ex@mplePAss");
        this.dbName = AirUtils.config.getString("sql.database", "Example_database");
    }

    /**
     * This will connect to the database for us and return the connection
     * @return The connection to the database
     */
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://"+ dbHost +"/"+ dbName + "?useUnicode=true&characterEncoding=UTF-8", user , pass);
        }
        catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * This will give the database name that we specified in the config
     * @return the database name
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * This will check if the database is connected
     * @return true if we are connected
     */
    public boolean checkDbConn() {
        return getConnection() != null;
    }

    /**
     * This will check if we have some settings in the databse
     * @return true if every sql field is set
     */
    public boolean hasSettings() {
        try {
            return !dbHost.isEmpty() && !user.isEmpty() && !pass.isEmpty() && !dbName.isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }
}