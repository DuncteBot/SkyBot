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

package ml.duncte123.skybot.web.controllers.dashboard

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.utils.AirUtils.colorToInt
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebRouter
import ml.duncte123.skybot.web.getGuild
import ml.duncte123.skybot.web.getParamsMap
import ml.duncte123.skybot.web.toCBBool
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object BasicSettings {

    fun save(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val params = request.getParamsMap()

        var prefix = params["prefix"] ?: "db!"

        if (prefix.length > 10) {
            prefix = prefix.substring(0, 10)
        }

        val welcomeChannel = params["welcomeChannel"]
        val welcomeLeaveEnabled = params["welcomeChannelCB"].toCBBool()
        val autorole = params["autoRoleRole"]
        //val autoRoleEnabled = params["autoRoleRoleCB"]
        val announceTracks = params["announceTracks"].toCBBool()
        val allowAllToStop = params["allowAllToStop"].toCBBool()
        val color = colorToInt(params["embedColor"])
        var leaveTimeout = GuildSettingsUtils.toLong(params["leaveTimeout"]).toInt()

        if (leaveTimeout < 1 || leaveTimeout > 60) {
            leaveTimeout = 1
        }

        val guild = DunctebotGuild(request.getGuild(shardManager)!!, variables)
        guild.color = color

        val newSettings = guild.settings
            .setCustomPrefix(prefix)
            .setWelcomeLeaveChannel(GuildSettingsUtils.toLong(welcomeChannel))
            .setEnableJoinMessage(welcomeLeaveEnabled)
            .setAutoroleRole(GuildSettingsUtils.toLong(autorole))
            .setAnnounceTracks(announceTracks)
            .setLeaveTimeout(leaveTimeout)
            .setAllowAllToStop(allowAllToStop)

        guild.settings = newSettings

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
