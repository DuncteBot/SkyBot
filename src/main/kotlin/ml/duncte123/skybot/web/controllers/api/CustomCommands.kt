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

package ml.duncte123.skybot.web.controllers.api

import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.bot.sharding.ShardManager
import org.json.JSONObject
import spark.Request
import spark.Response

object CustomCommands {

    fun show(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        /*val attributes = request.session().attributes()

        if (!attributes.contains(WebRouter.USER_SESSION)) {
            return JSONObject()
                .put("status", "error")
                .put("message", "SESSION_INVALID")
                .put("code", response.status())
        }*/

        val guild = WebHelpers.getGuildFromRequest(request, shardManager)

        val commands = variables.commandManager
            .getCustomCommands(guild!!.idLong).map { it.toJSONObject() }

        return JSONObject()
            .put("status", "success")
            .put("commands", commands)
            .put("code", response.status())
    }

}
