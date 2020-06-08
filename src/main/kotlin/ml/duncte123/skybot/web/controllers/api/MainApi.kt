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

import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.Author
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object MainApi {

    fun serverCount(response: Response, shardManager: ShardManager, mapper: ObjectMapper): Any {
        return mapper.createObjectNode()
            .put("status", "success")
            .put("server_count", shardManager.guildCache.size())
            .put("shard_count", shardManager.shardsTotal)
            .put("code", response.status())
    }
}
