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
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHolder
import org.apache.http.client.utils.URLEncodedUtils
import spark.Spark.path
import spark.kotlin.post
import java.nio.charset.Charset

@Author(nickname = "duncte123", author = "Duncan Sterken")
class MessageSettings(private val holder: WebHolder) {

    init {
        path("/server/:guildid") {

            // Messages
            holder.get("/messages", WebVariables()
                .put("title", "Dashboard"), "dashboard/welcomeLeaveDesc.twig", true)

            post("/messages") {
                val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
                val params = holder.toMap(pairs)

                val welcomeLeaveEnabled = holder.paramToBoolean(params["welcomeChannelCB"])
                val welcomeMessage = params["welcomeMessage"]
                val leaveMessage = params["leaveMessage"]
                val serverDescription = params["serverDescription"]
                val welcomeChannel = params["welcomeChannel"]

                val guild = holder.getGuildFromRequest(request)

                val newSettings = GuildSettingsUtils.getGuild(guild, holder.variables)
                    .setServerDesc(serverDescription)
                    .setWelcomeLeaveChannel(GuildSettingsUtils.toLong(welcomeChannel))
                    .setCustomJoinMessage(welcomeMessage)
                    .setCustomLeaveMessage(leaveMessage)
                    .setEnableJoinMessage(welcomeLeaveEnabled)

                GuildSettingsUtils.updateGuildSettings(guild, newSettings, holder.variables)

                request.session().attribute(holder.FLASH_MESSAGE, "<h4>Settings updated</h4>")

                response.redirect(request.url())
            }
        }
    }

}
