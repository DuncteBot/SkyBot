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

package ml.duncte123.skybot.web.controllers.api

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.commands.guild.owner.CustomCommandCommand.*
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.bot.sharding.ShardManager
import org.json.JSONObject
import spark.Request
import spark.Response
import spark.kotlin.halt

@Author(nickname = "duncte123", author = "Duncan Sterken")
object CustomCommands {

    fun before(request: Request, response: Response) {
        val attributes = request.session().attributes()

        if (!attributes.contains(WebRouter.USER_SESSION)) {
            response.status(401)

            halt(401,
                JSONObject()
                    .put("status", "error")
                    .put("message", "Invalid session")
                    .put("code", response.status())
                    .toString()
            )
        }
    }

    fun show(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val guild = WebHelpers.getGuildFromRequest(request, shardManager) ?: return JSONObject()
            .put("status", "error")
            .put("message", "guild not found")
            .put("code", response.status())

        val commands = variables.commandManager
            .getCustomCommands(guild.idLong).map { it.toJSONObject() }

        return JSONObject()
            .put("status", "success")
            .put("commands", commands)
            .put("code", response.status())
    }

    fun update(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {

        val guild = WebHelpers.getGuildFromRequest(request, shardManager) ?: return JSONObject()
            .put("status", "error")
            .put("message", "guild not found")
            .put("code", response.status())

        val commandData = JSONObject(request.body())

        if (!commandData.has("name") || !commandData.has("message")) {
            response.status(403)

            return JSONObject()
                .put("status", "error")
                .put("message", "Invalid data")
                .put("code", response.status())
        }

        val invoke = commandData.getString("name")
        val message = commandData.getString("message")
        val autoresponse = commandData.optBoolean("autoresponse", false)
        val manager = variables.commandManager

        if (!commandExists(invoke, guild.idLong, manager)) {
            response.status(404)

            return JSONObject()
                .put("status", "error")
                .put("message", "Unknown command")
                .put("code", response.status())
        }

        val customCommand = manager.getCustomCommand(invoke, guild.idLong)

        val returnData = editCustomCommand(customCommand, message, autoresponse, manager)

        if (!returnData) {
            return JSONObject()
                .put("status", "error")
                .put("message", "Something failed")
                .put("code", response.status())
        }

        return JSONObject()
            .put("status", "success")
            .put("code", response.status())
    }

    fun create(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val guild = WebHelpers.getGuildFromRequest(request, shardManager)!!
        val commandData = JSONObject(request.body())

        if (!commandData.has("name") || !commandData.has("message") || !commandData.has("autoresponse")) {
            response.status(403)

            return JSONObject()
                .put("status", "error")
                .put("message", "Invalid data")
                .put("code", response.status())
        }

        var invoke = commandData.getString("name")
        invoke = invoke.replace("\\s".toRegex(), "")


        if (invoke.length > 25) {
            return JSONObject()
                .put("status", "error")
                .put("message", "Invoke is over 25 characters")
                .put("code", response.status())
        }

        val message = commandData.getString("message")

        if (message.length > 4000) {
            return JSONObject()
                .put("status", "error")
                .put("message", "Message is over 4000 characters")
                .put("code", response.status())
        }

        val manager = variables.commandManager

        if (commandExists(invoke, guild.idLong, manager)) {
            response.status(404)

            return JSONObject()
                .put("status", "error")
                .put("message", "Command already exists")
                .put("code", response.status())
        }
        val autoresponse = commandData.getBoolean("autoresponse")

        val result = registerCustomCommand(invoke, message, guild.idLong, autoresponse, manager)

        if (result.first) {
            return JSONObject()
                .put("status", "success")
                .put("message", "Command added")
                .put("code", response.status())
        }

        if (result.second) {
            return JSONObject()
                .put("status", "error")
                .put("message", "Command already exists")
                .put("code", response.status())
        }

        if (result.third) {
            return JSONObject()
                .put("status", "error")
                .put("message", "You reached the limit of 50 custom commands for this server")
                .put("code", response.status())
        }

        return JSONObject()
            .put("status", "error")
            .put("message", "Database error")
            .put("code", response.status())
    }

    fun delete(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val guild = WebHelpers.getGuildFromRequest(request, shardManager)!!
        val commandData = JSONObject(request.body())

        if (!commandData.has("name")) {
            response.status(403)

            return JSONObject()
                .put("status", "error")
                .put("message", "Invalid data")
                .put("code", response.status())
        }

        val invoke = commandData.getString("name")

        val manager = variables.commandManager

        if (!commandExists(invoke, guild.idLong, manager)) {
            response.status(404)

            return JSONObject()
                .put("status", "error")
                .put("message", "Command does not exists")
                .put("code", response.status())
        }

        val success = manager.removeCustomCommand(invoke, guild.idLong)

        if (!success) {
            return JSONObject()
                .put("status", "error")
                .put("message", "Could not delete command")
                .put("code", response.status())
        }

        return JSONObject()
            .put("status", "success")
            .put("message", "Command deleted")
            .put("code", response.status())
    }
}
