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

import com.fasterxml.jackson.databind.JsonNode
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.web.WebSocketClient
import ml.duncte123.skybot.websocket.SocketHandler
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.data.DataArray
import net.dv8tion.jda.api.utils.data.DataObject

class RequestHandler(private val variables: Variables, private val shardManager: ShardManager, client: WebSocketClient) : SocketHandler(client) {
    override fun handleInternally(data: JsonNode) {
        val responseData = DataObject.empty()
            .put("identifier", data["identifier"].asText())

        if (data.has("partial_guilds") && data["partial_guilds"].isArray) {
            //
        }

        client.send(
            DataObject.empty()
                .put("t", "FETCH_DATA")
                .put("d", responseData)
        )
    }

    fun fetchGuilds(guildIds: JsonNode) {
        val guilds = DataArray.empty()

        guildIds.forEach {
            val guildById = shardManager.getGuildById(it.asLong())
            val guildData = DataObject.empty()
                .put("id", it.asText())
                .put("member_count", -1)

            if (guildById != null) {
                guildData.put("member_count", guildById.memberCount)
            }

            guilds.add(guildData)
        }

    }
}
