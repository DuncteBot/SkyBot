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

import ml.duncte123.skybot.utils.Variables;

import java.io.File;
import java.sql.Connection;
import java.util.concurrent.*;

public class DBManager {

    public final DBConnectionManager connManager;
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1,
            r -> new Thread(r, "SQL-thread"));
    /**
     * This is the database name
     */
    private final String name;

    /**
     * This will set our stuff up
     */
    public DBManager() {
        this.connManager = createDBManager();
        this.name = connManager.getName();
    }

    private static DBConnectionManager createDBManager() {
        if (Variables.NONE_SQLITE) return new MySQLConnectionManager();
        return new SQLiteDatabaseConnectionManager(new File("database.db"));
    }

    /**
     * This will check the connection for us
     *
     * @return true if we are connected
     */
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
     * @deprecated use {@link #getConnManager()} instead
     */
    @Deprecated
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

    public <T> ScheduledFuture<T> run(Callable<T> c) {
        return service.schedule(c, 0L, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> run(Runnable r) {
        return service.schedule(r, 0L, TimeUnit.MILLISECONDS);
    }

    public ScheduledExecutorService getService() {
        return service;
    }
}
