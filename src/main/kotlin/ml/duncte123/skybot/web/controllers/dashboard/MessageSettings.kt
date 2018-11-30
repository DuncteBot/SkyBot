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

package ml.duncte123.skybot.web.controllers.dashboard

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.bot.sharding.ShardManager
import org.apache.http.client.utils.URLEncodedUtils
import spark.Request
import spark.Response
import java.nio.charset.StandardCharsets

@Author(nickname = "duncte123", author = "Duncan Sterken")
object MessageSettings {

    fun save(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val pairs = URLEncodedUtils.parse(request.body(), StandardCharsets.UTF_8)
        val params = WebHelpers.toMap(pairs)

        val welcomeLeaveEnabled = WebHelpers.paramToBoolean(params["welcomeChannelCB"])
        val welcomeMessage = params["welcomeMessage"]
        val leaveMessage = params["leaveMessage"]
        val serverDescription = params["serverDescription"]
        val welcomeChannel = params["welcomeChannel"]

        val guild = WebHelpers.getGuildFromRequest(request, shardManager)

        val newSettings = GuildSettingsUtils.getGuild(guild, variables)
            .setServerDesc(serverDescription)
            .setWelcomeLeaveChannel(GuildSettingsUtils.toLong(welcomeChannel))
            .setCustomJoinMessage(welcomeMessage)
            .setCustomLeaveMessage(leaveMessage)
            .setEnableJoinMessage(welcomeLeaveEnabled)

        GuildSettingsUtils.updateGuildSettings(guild, newSettings, variables)

        request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Settings updated</h4>")

        return response.redirect(request.url())
    }
}
