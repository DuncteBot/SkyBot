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

package ml.duncte123.skybot.web.controllers

import com.github.benmanes.caffeine.cache.Caffeine
import ml.duncte123.skybot.objects.web.WebVariables
import ml.duncte123.skybot.web.WebHelpers
import net.dv8tion.jda.api.sharding.ShardManager
import spark.*
import java.util.concurrent.TimeUnit

object GuildStuffController {
    val guildHashes = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.HOURS)
        .build<String, Long>()

    fun showGuildRoles(request: Request, response: Response, shardManager: ShardManager): Any {
        val hash = request.params("hash")
        val guildId = guildHashes.getIfPresent(hash) ?: return WebHelpers.haltNotFound(request, response)
        val guild = shardManager.getGuildById(guildId) ?: return WebHelpers.haltNotFound(request, response)

        return WebVariables()
            .put("title", "Roles for ${guild.name}")
            .put("guild_name", guild.name)
            .put("roles", guild.roles)
            .toModelAndView("guildRoles.vm")
    }
}
