/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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