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

package ml.duncte123.skybot.adapters

import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import java.sql.SQLException

class SqliteDatabaseAdapter(private val variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        val database = variables.database

        database.run {
            val customCommands = arrayListOf<CustomCommand>()

            try {
                database.connManager.use { manager ->

                    val con = manager.connection

                    val res = con.createStatement().executeQuery("SELECT invoke, message, guildId FROM customCommands")
                    while (res.next()) {
                        customCommands.add(CustomCommandImpl(
                            res.getString("invoke"),
                            res.getString("message"),
                            res.getLong("guildId")
                        ))
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            callback.invoke(customCommands)
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            val res = changeCommand(guildId, invoke, message, false)

            callback.invoke(res)
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            val res = changeCommand(guildId, invoke, message, true)

            callback.invoke(res)
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit) {
        val database = variables.database

        val ret = database.run<Boolean> {

            try {
                database.connManager.use { manager ->
                    val con = manager.connection

                    val stm = con.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?")
                    stm.setString(1, invoke)
                    stm.setString(2, guildId.toString())
                    stm.execute()
                }

            } catch (e: SQLException) {
                e.printStackTrace()
                return@run false
            }

            return@run true
        }.get()

        callback.invoke(ret)
    }

    private fun changeCommand(guildId: Long, invoke: String, message: String, isEdit: Boolean): Triple<Boolean, Boolean, Boolean>? {
        val database = variables.database

        val sqlQuerry = if (isEdit)
            "UPDATE customCommands SET message = ? WHERE guildId = ? AND invoke = ?"
        else
            "INSERT INTO customCommands(guildId, invoke, message) VALUES (? , ? , ?)"

        try {
            database.connManager.use { manager ->
                val conn = manager.connection

                val stm = conn.prepareStatement(sqlQuerry)

                stm.setString(if (isEdit) 2 else 1, guildId.toString())
                stm.setString(if (isEdit) 3 else 2, invoke)
                stm.setString(if (isEdit) 1 else 3, message)
                stm.execute()
            }
        }
        catch (e: SQLException) {
            e.printStackTrace()
            return Triple(false, false, false)
        }

        return null
    }
}
