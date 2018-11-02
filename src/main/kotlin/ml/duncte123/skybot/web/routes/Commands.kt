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

package ml.duncte123.skybot.web.routes

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHolder
import spark.ModelAndView
import spark.kotlin.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class Commands(private val holder: WebHolder) {

    init {
        get("/commands", DEFAULT_ACCEPT, holder.engine) {
            val map = WebVariables().put("title", "List of commands").put("prefix", Settings.PREFIX)
                .put("commands", holder.variables.commandManager.sortedCommands)

            if (request.queryParams().contains("server")) {
                val serverId: String = request.queryParams("server")
                if (serverId.isNotEmpty()) {
                    val guild = holder.shardManager.getGuildById(serverId)
                    if (guild != null) {
                        val settings = GuildSettingsUtils.getGuild(guild, holder.variables)
                        map.put("prefix", settings.customPrefix)
                    }
                }
            }

            map.put("color", AirUtils.colorToHex(Settings.defaultColour))

            ModelAndView(map.map, "commands.twig")
        }
    }

}
