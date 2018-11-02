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
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHolder
import org.apache.http.client.utils.URLEncodedUtils
import spark.Spark.path
import spark.kotlin.post
import java.awt.Color
import java.nio.charset.Charset

@Author(nickname = "duncte123", author = "Duncan Sterken")
class BasicSettings(private val holder: WebHolder) {

    init {
        path("/server/:guildid") {
            // Overview and editing
            holder.get("/basic", WebVariables()
                .put("title", "Dashboard"), "dashboard/basicSettings.twig", true)

            post("/basic") {
                val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
                val params = holder.toMap(pairs)

                val prefix = params["prefix"]
                val welcomeChannel = params["welcomeChannel"]
                val welcomeLeaveEnabled = holder.paramToBoolean(params["welcomeChannelCB"])
                val autorole = params["autoRoleRole"]
                //val autoRoleEnabled      = params["autoRoleRoleCB"]
                val announceTracks = holder.paramToBoolean(params["announceTracks"])
                val color = Color.decode(params["embedColor"]).rgb

                val guild = DunctebotGuild(holder.getGuildFromRequest(request)!!, holder.variables)
                guild.setColor(color)

                val newSettings = GuildSettingsUtils.getGuild(guild, holder.variables)
                    .setCustomPrefix(prefix)
                    .setWelcomeLeaveChannel(GuildSettingsUtils.toLong(welcomeChannel))
                    .setEnableJoinMessage(welcomeLeaveEnabled)
                    .setAutoroleRole(GuildSettingsUtils.toLong(autorole))
                    .setAnnounceTracks(announceTracks)

                GuildSettingsUtils.updateGuildSettings(guild, newSettings, holder.variables)

                request.session().attribute(holder.FLASH_MESSAGE, "<h4>Settings updated</h4>")

                response.redirect(request.url())
            }
        }
    }

}
