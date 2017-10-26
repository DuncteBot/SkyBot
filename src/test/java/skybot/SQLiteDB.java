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

package skybot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteDB {

    public static void main(String[] args)
    throws Throwable {
        // Get ../resources/test.db
        String url = "jdbc:sqlite:" +
                   new File(new File(System.getProperty("user.dir"))
                       .getParentFile(), "resources")
                   + File.pathSeparator + "test.db";
        
        System.out.println(url);
        
        Connection con = DriverManager.getConnection(url);
        
        Statement s = con.createStatement();
        
        s.execute("CREATE TABLE IF NOT EXISTS test (id int PRIMARY KEY)");
        
        s.execute("DROP TABLE test");
        
        s.close();
        
        System.out.println(con);
        
        con.close();
    }
}
