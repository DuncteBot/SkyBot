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
import ml.duncte123.skybot.objects.guild.WarnAction
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebRouter
import ml.duncte123.skybot.web.getGuild
import ml.duncte123.skybot.web.getParamsMap
import ml.duncte123.skybot.web.toCBBool
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request
import spark.Response
import spark.Spark.halt
import kotlin.math.max
import kotlin.math.min

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

        val aiSensitivity = ((params["ai-sensitivity"] ?: "0.7").toFloatOrNull() ?: 0.7f).minMax(0f, 1f)

        for (i in 0..5) {
            val reqItemId = i + 1
            val value = params.getValue("rateLimits[$reqItemId]")

            if (value.isEmpty()) {
                request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Rate limits are invalid</h4>")

                return response.redirect(request.url())
            }

            rateLimits[i] = value.toLong()
        }

        val guild = request.getGuild(shardManager)!!
        val isGuildPatron = CommandUtils.isGuildPatron(guild)
        val warnActionsCount = if(isGuildPatron) 3 else 1
        val warnActionsList = ArrayList<WarnAction>(warnActionsCount)

        for (i in 1 until warnActionsCount + 1) {
            if (!params.containsKey("warningAction$i") ||
                !params.containsKey("tempDays$i") ||
                !params.containsKey("threshold$i")) {
                halt(400, "Invalid warn action detected")
            }

            val action = WarnAction.Type.valueOf(params.getValue("warningAction$i"))
            val duration = params.getValue("tempDays$i").toInt()
            val threshold = params.getValue("threshold$i").toInt()

            warnActionsList.add(WarnAction(action, threshold, duration))
        }

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
            .setAiSensitivity(aiSensitivity)
            .setWarnActions(warnActionsList)

        GuildSettingsUtils.updateGuildSettings(guild, newSettings, variables)

        request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Settings updated</h4>")

        return response.redirect(request.url())
    }

    private fun Float.minMax(min: Float, max: Float): Float {
        // max returns the highest value and min returns the lowest value
        return min(max, max(min, this))
    }
}
