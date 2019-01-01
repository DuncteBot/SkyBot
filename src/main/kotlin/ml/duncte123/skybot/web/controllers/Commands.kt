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

package ml.duncte123.skybot.web.controllers

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.bot.sharding.ShardManager
import spark.ModelAndView
import spark.Request

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Commands {

    fun show(request: Request, variables: Variables, shardManager: ShardManager): Any {
        val map = WebVariables().put("title", "List of commands").put("prefix", Settings.PREFIX)
            .put("commands", variables.commandManager.sortedCommands)

        if (request.queryParams().contains("server")) {
            val serverId: String = request.queryParams("server")
            if (serverId.isNotEmpty()) {
                val guild = shardManager.getGuildById(serverId)

                if (guild != null) {
                    val settings = GuildSettingsUtils.getGuild(guild, variables)
                    map.put("prefix", settings.customPrefix)
                }
            }
        }

        map.put("color", AirUtils.colorToHex(Settings.defaultColour))

        return ModelAndView(map.map, "commands.twig")
    }

}
