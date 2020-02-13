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

package ml.duncte123.skybot.web.controllers.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.benmanes.caffeine.cache.Caffeine
import com.jagrosh.jdautilities.oauth2.OAuth2Client
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.web.WebRouter
import ml.duncte123.skybot.web.getSession
import ml.duncte123.skybot.web.getUserId
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Response
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

@Author(nickname = "duncte123", author = "Duncan Sterken")
object GetUserGuilds {
    private val guildsRequests = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, List<OAuth2Guild>>()


    fun show(request: Request, response: Response, oAuth2Client: OAuth2Client, shardManager: ShardManager, mapper: ObjectMapper): Any {
        val attributes = request.session().attributes()

        // Since we're not accessing the cache we need to call this method manually
        // All this does is remove expired entries from the cache
        guildsRequests.cleanUp()

        // We need to make sure that we are logged in and have a user id
        // If we don't have either of them we will return an error message
        if (!attributes.contains(WebRouter.USER_ID) || !attributes.contains(WebRouter.SESSION_ID)) {
            request.session().invalidate()

            return mapper.createObjectNode()
                .put("status", "error")
                .put("message", "SESSION_INVALID")
                .put("code", response.status())
        }

        val guilds = mapper.createArrayNode()

        // Attempt to get all guilds from the cache
        // We are using a cache here to make sure we don't rate limit our application
        val guildsRequest = guildsRequests.get(request.getUserId()) {
            return@get oAuth2Client.getGuilds(request.getSession(oAuth2Client)).complete()
        }!!

        guildsRequest.forEach {
            // Only add the servers where the user has the MANAGE_SERVER
            // perms to the list
            if (it.hasPermission(Permission.MANAGE_SERVER)) {
                guilds.add(guildToJson(it, shardManager, mapper))
            }
        }

        return mapper.createObjectNode()
            .put("status", "success")
            .put("total", guildsRequest.size)
            .put("code", response.status())
            .set<ObjectNode>("guilds", guilds)
    }

    private fun guildToJson(guild: OAuth2Guild, shardManager: ShardManager, mapper: ObjectMapper): JsonNode {
        // Try and get a guild from our JDA cache
        // This "real" guild object is used to get the member count display on the dashboard
        val jdaGuild = shardManager.getGuildById(guild.id)

        // Get guild id or random default avatar url
        val icon = if (!guild.iconUrl.isNullOrEmpty()) {
            guild.iconUrl
        } else {
            val number = ThreadLocalRandom.current().nextInt(0, 5)
            "https://cdn.discordapp.com/embed/avatars/$number.png"
        }

        return mapper.createObjectNode()
            .put("name", guild.name)
            .put("iconId", guild.iconId)
            .put("iconUrl", icon)
            .put("owner", guild.isOwner)
            .put("members", jdaGuild?.memberCount ?: -1)
            .put("id", guild.id)
    }
}
