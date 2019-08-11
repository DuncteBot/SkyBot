/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.utils.AirUtils.colorToInt
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebHelpers.paramToBoolean
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.api.sharding.ShardManager
import org.apache.http.client.utils.URLEncodedUtils
import spark.Request
import spark.Response
import java.nio.charset.StandardCharsets

@Author(nickname = "duncte123", author = "Duncan Sterken")
object BasicSettings {

    fun save(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val pairs = URLEncodedUtils.parse(request.body(), StandardCharsets.UTF_8)
        val params = WebHelpers.toMap(pairs)

        val prefix = params["prefix"]
        val welcomeChannel = params["welcomeChannel"]
        val welcomeLeaveEnabled = paramToBoolean(params["welcomeChannelCB"])
        val autorole = params["autoRoleRole"]
        //val autoRoleEnabled = params["autoRoleRoleCB"]
        val announceTracks = paramToBoolean(params["announceTracks"])
        val color = colorToInt(params["embedColor"])
        var leaveTimeout = GuildSettingsUtils.toLong(params["leaveTimeout"]).toInt()

        if (leaveTimeout < 1 || leaveTimeout > 60) {
            leaveTimeout = 1
        }

        val guild = DunctebotGuild(WebHelpers.getGuildFromRequest(request, shardManager)!!, variables)
        guild.setColor(color)

        val newSettings = GuildSettingsUtils.getGuild(guild, variables)
            .setCustomPrefix(prefix)
            .setWelcomeLeaveChannel(GuildSettingsUtils.toLong(welcomeChannel))
            .setEnableJoinMessage(welcomeLeaveEnabled)
            .setAutoroleRole(GuildSettingsUtils.toLong(autorole))
            .setAnnounceTracks(announceTracks)
            .setLeaveTimeout(leaveTimeout)

        GuildSettingsUtils.updateGuildSettings(guild, newSettings, variables)

        /* val autoVcRoleVc = (params["vcAutoRoleVc"] ?: "0").toLong()
         val autoVcRoleRole = (params["vcAutoRoleRole"] ?: "0").toLong()
         val cache = variables.vcAutoRoleCache.get(guild.idLong)
             ?: variables.vcAutoRoleCache.put(guild.idLong, TLongLongHashMap())

         if (autoVcRoleVc != 0L && autoVcRoleRole != 0L) {
             variables.databaseAdapter.setVcAutoRole(guild.idLong, autoVcRoleVc, autoVcRoleRole)
             cache.put(autoVcRoleVc, autoVcRoleRole)
         } else if (variables.vcAutoRoleCache.containsKey(guild.idLong)) {
             // TODO: Refactor
             val stored = variables.vcAutoRoleCache.remove(guild.idLong)
             variables.databaseAdapter.removeVcAutoRole(stored.voiceChannelId)
         }*/

        request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Settings updated</h4>")

        return response.redirect(request.url())
    }
}
