/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.web

import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.utils.GuildSettingsUtils
import spark.kotlin.*

/**
 * Notes:
 * duncte123: we're gonna use this for templating https://github.com/warhuhn/warhuhn-spark-template-jtwig
 * it's twig and it's easy to use
 */

class WebServer {

    val http = ignite()

    fun activate() {
        //Port has to be 2000 because of the apache proxy on the vps
        http.port(2000)

        http.get("/") {
            "Hello world"
        }

        http.get("/api/servers") {
            "Server count: ${SkyBot.getInstance().shardManager.guildCache.size()}"
        }

        http.get("/server/:guildid") {
            val guild = SkyBot.getInstance()
                    .shardManager.getGuildById(request.params(":guildid"))
            if(guild == null) {
                "This server was not found"
            } else {
                val settings = GuildSettingsUtils.getGuild(guild)
                """<p>Guild prefix: ${settings.customPrefix}</p>
                |<p>Join Message: ${settings.customJoinMessage}</p>
            """.trimMargin()
            }
        }

        http.notFound {
            "This page could not be found"
        }
    }

}