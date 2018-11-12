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
import ml.duncte123.skybot.web.WebHolder
import spark.Spark.path
import spark.kotlin.get

@Author(nickname = "duncte123", author = "Duncan Sterken")
class MusicSettings(private val holder: WebHolder) {

    init {
        path("/server/:guildid") {
            //audio stuff
            get("/music") {
                val guild = holder.getGuildFromRequest(request)
                if (guild != null) {
                    val mng = holder.audioUtils.getMusicManager(guild, false)

                    if (mng != null) {
                        return@get """<p>Audio player details:</p>
                            |<p>Currently playing: <b>${if (mng.player.playingTrack != null) mng.player.playingTrack.info.title else "nothing"}</b></p>
                            |<p>Total tracks in queue: <b>${mng.scheduler.queue.size}</b></p>
                        """.trimMargin()
                    } else {
                        return@get "The audio player does not seem to be active"
                    }
                } else {
                    return@get "ERROR"
                }
            }
        }
    }

}
