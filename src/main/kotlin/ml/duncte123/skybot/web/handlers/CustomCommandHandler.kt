/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.web.handlers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.web.WebSocketClient
import ml.duncte123.skybot.websocket.SocketHandler

class CustomCommandHandler(variables: Variables, client: WebSocketClient) : SocketHandler(client) {
    private val manager = variables.commandManager
    private val jackson = variables.jackson

    override fun handleInternally(data: JsonNode) {
        if (data.has("remove")) {
            removeCustomCommands(data["remove"])
        }

        if (data.has("update")) {
            updateCustomCommands(data["update"])
        }

        if (data.has("add")) {
            addCustomCommands(data["add"])
        }
    }

    private fun removeCustomCommands(commands: JsonNode) {
        commands.forEach {
            manager.removeCustomCommand(it["invoke"].asText(), it["guild_id"].asLong())
        }
    }

    private fun updateCustomCommands(raw: JsonNode) {
        val commands = jackson.readValue(raw.traverse(), object : TypeReference<List<CustomCommandImpl>>() {})

        commands.forEach {
            manager.editCustomCommand(it)
        }
    }

    private fun addCustomCommands(raw: JsonNode) {
        val commands = jackson.readValue(raw.traverse(), object : TypeReference<List<CustomCommandImpl>>() {})

        commands.forEach {
            manager.registerCustomCommand(it)
        }
    }
}
