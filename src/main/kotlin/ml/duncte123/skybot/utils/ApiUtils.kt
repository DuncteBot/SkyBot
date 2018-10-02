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

package ml.duncte123.skybot.utils

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.connections.database.DBManager
import ml.duncte123.skybot.objects.api.KpopObject
import ml.duncte123.skybot.objects.api.LlamaObject
import ml.duncte123.skybot.objects.api.WarnObject
import ml.duncte123.skybot.objects.api.Warning
import java.sql.ResultSet
import java.sql.SQLException

@Author(nickname = "duncte123", author = "Duncan Sterken")
object ApiUtils {

    @JvmStatic
    fun getRandomLlama(database: DBManager): LlamaObject {

        val conn = database.connManager.connection

        val resultSet = conn.createStatement()
            .executeQuery("SELECT * FROM animal_apis ORDER BY RAND() LIMIT 1")
        resultSet.next()
        val obj = LlamaObject(resultSet.getInt("id"), resultSet.getString("file"))
        conn.close()

        return obj
    }

    @JvmStatic
    fun getRandomKpopMember(database: DBManager, search: String = ""): KpopObject {

        val conn = database.connManager.connection

        lateinit var resultSet: ResultSet
        resultSet = if (!search.isEmpty()) {
            val stmt = conn.prepareStatement("SELECT * FROM kpop WHERE name LIKE ? OR id=? LIMIT 1")
            stmt.setString(1, "%$search%")
            stmt.setString(2, search)
            stmt.executeQuery()
        } else {
            conn.createStatement().executeQuery("SELECT * FROM kpop ORDER BY RAND() LIMIT 1")
        }
        resultSet.next()
        val obj = KpopObject(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getString("band"),
            resultSet.getString("img")
        )
        conn.close()

        return obj
    }

    @JvmStatic
    fun getWarnsForUser(database: DBManager, userId: String, guildId: String): WarnObject {
        val conn = database.connManager.connection
        val warnings = ArrayList<Warning>()

        try {
            val smt = conn.prepareStatement(
                "SELECT * FROM `warnings` WHERE user_id=? AND guild_id=? AND (CURDATE() <= DATE_ADD(expire_date, INTERVAL 3 DAY))")
            smt.setString(1, userId)
            smt.setString(2, guildId)
            val result = smt.executeQuery()

            while (result.next()) {
                warnings.add(Warning(
                    result.getInt("id"),
                    result.getDate("warn_date"),
                    result.getDate("expire_date"),
                    result.getString("mod_id"),
                    result.getString("reason"),
                    result.getString("guild_id")
                ))
            }

            conn.close()
            return WarnObject(userId, warnings)
        } catch (e: SQLException) {
            conn.close()
            return WarnObject(userId, warnings)
        }
    }

}
