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
import org.json.JSONObject
import java.lang.Exception

class WebDatabaseAdapter(private val variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {

        variables.database.run {
            try {

                val customCommands = arrayListOf<CustomCommand>()

                val array = variables.apis.getCustomCommands()

                array.forEach { c ->
                    val j = c as JSONObject

                    customCommands.add(CustomCommandImpl(
                        j.getString("invoke"),
                        j.getString("message"),
                        j.getLong("guildId")
                    ))
                }

                callback.invoke(customCommands)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(variables.apis.deleteCustomCommand(guildId, invoke))
        }
    }
}
