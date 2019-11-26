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
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebRouter
import ml.duncte123.skybot.web.getGuild
import ml.duncte123.skybot.web.getParamsMap
import ml.duncte123.skybot.web.toCBBool
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object ModerationSettings {

    fun save(request: Request, response: Response, shardManager: ShardManager, variables: Variables): Any {
        val params = request.getParamsMap()

        val modLogChannel = params["modChannel"]
        val autoDeHoist = params["autoDeHoist"].toCBBool()
        val filterInvites = params["filterInvites"].toCBBool()
        val swearFilter = params["swearFilter"].toCBBool()
        val muteRole = params["muteRole"]
        val spamFilter = params["spamFilter"].toCBBool()
        val kickMode = params["kickMode"].toCBBool()
        val spamThreshold = (params["spamThreshold"] ?: "7").toInt()
        val filterType = params["filterType"]
        val rateLimits = LongArray(6)

        val logBan = params["logBan"].toCBBool()
        val logUnban = params["logUnban"].toCBBool()
        val logMute = params["logMute"].toCBBool()
        val logKick = params["logKick"].toCBBool()
        val logWarn = params["logWarn"].toCBBool()

        for (i in 0..5) {

            val value = params.getValue("rateLimits[$i]")

            if (value.isEmpty()) {
                request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Invalid settings detected</h4>")

                return response.redirect(request.url())
            }

            rateLimits[i] = value.toLong()
        }

        val guild = request.getGuild(shardManager)

        val newSettings = GuildSettingsUtils.getGuild(guild, variables)
            .setLogChannel(GuildSettingsUtils.toLong(modLogChannel))
            .setAutoDeHoist(autoDeHoist)
            .setFilterInvites(filterInvites)
            .setMuteRoleId(GuildSettingsUtils.toLong(muteRole))
            .setKickState(kickMode)
            .setRatelimits(rateLimits)
            .setEnableSpamFilter(spamFilter)
            .setEnableSwearFilter(swearFilter)
            .setSpamThreshold(spamThreshold)
            .setFilterType(filterType)
            .setBanLogging(logBan)
            .setUnbanLogging(logUnban)
            .setMuteLogging(logMute)
            .setKickLogging(logKick)
            .setWarnLogging(logWarn)

        GuildSettingsUtils.updateGuildSettings(guild, newSettings, variables)

        request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Settings updated</h4>")

        return response.redirect(request.url())
    }
}
