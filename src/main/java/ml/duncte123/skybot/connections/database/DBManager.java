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

import java.io.File;
import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class DBManager {

    private final DBConnectionManager connManager;
    private final ExecutorService service = Executors.newCachedThreadPool(r -> new Thread(r, "SQL-thread"));
    /**
     * This is the database name
     */
    private final String name;

    /**
     * This will set our stuff up
     */
    public DBManager(boolean isSql) {
        this.connManager = createDBManager(isSql);

        if (this.connManager != null) {
            this.name = connManager.getName();
        } else {
            this.name = "No_Db";
        }
    }

    private DBConnectionManager createDBManager(boolean isSql) {
        if (!isSql) {
            return new SQLiteDatabaseConnectionManager(new File("database.db"));
        }

        return null;
    }

    /**
     * This will check the connection for us
     *
     * @return true if we are connected
     */
    @SuppressWarnings("unused")
    public boolean isConnected() {
        return connManager.isConnected();
    }

    /**
     * This will return the name of the connected database
     *
     * @return The name of the connected database
     */
    public String getName() {
        return this.name;
    }

    /**
     * This will get the connection for us
     *
     * @return the connection, will we null if we aren't connected
     */
    @SuppressWarnings("unused")
    public Connection getConnection() {
        return connManager.getConnection();
    }

    /**
     * Returns the connection manager
     *
     * @return the {@link DBConnectionManager DatabaseConnectionManager}
     */
    public DBConnectionManager getConnManager() {
        return connManager;
    }

    public <T> Future<T> run(Callable<T> c) {
        return service.submit(c);
    }

    public Future<?> run(Runnable r) {
        return service.submit(r);
    }

    public ExecutorService getService() {
        return service;
    }
}
