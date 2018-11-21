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

package ml.duncte123.skybot.web.routes.dashboard

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.web.WebHelpers
import net.dv8tion.jda.bot.sharding.ShardManager
import spark.Request

@Author(nickname = "duncte123", author = "Duncan Sterken")
object MusicSettings {

    fun show(request: Request, shardManager: ShardManager, variables: Variables): Any {
        val guild = WebHelpers.getGuildFromRequest(request, shardManager)
            ?: return "Guild does not exist"

        val mng = variables.audioUtils.getMusicManager(guild, false)
            ?: return "The audio player does not seem to be active"

        return """<p>Audio player details:</p>
            |<p>Currently playing: <b>${if (mng.player.playingTrack != null) mng.player.playingTrack.info.title else "nothing"}</b></p>
            |<p>Total tracks in queue: <b>${mng.scheduler.queue.size}</b></p>
        """.trimMargin()
    }
}
