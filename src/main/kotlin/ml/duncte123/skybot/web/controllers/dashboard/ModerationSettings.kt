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
import net.dv8tion.jda.api.entities.Guild
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

        val logBan = params["logBan"].toCBBool()
        val logUnban = params["logUnban"].toCBBool()
        val logMute = params["logMute"].toCBBool()
        val logKick = params["logKick"].toCBBool()
        val logWarn = params["logWarn"].toCBBool()

        val aiSensitivity = ((params["ai-sensitivity"] ?: "0.7").toFloatOrNull() ?: 0.7f).minMax(0f, 1f)
        val rateLimits = parseRateLimits(request, params) ?: return response.redirect(request.url())

        val guild = request.getGuild(shardManager)!!
        val guildId = guild.idLong
        val warnActionsList = parseWarnActions(guild, params)

        val newSettings = GuildSettingsUtils.getGuild(guildId, variables)
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

        GuildSettingsUtils.updateGuildSettings(guildId, newSettings, variables)

        variables.databaseAdapter.setWarnActions(guildId, newSettings.warnActions)

        request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Settings updated</h4>")

        return response.redirect(request.url())
    }

    private fun parseRateLimits(request: Request, params: Map<String, String>): LongArray? {
        val rateLimits = LongArray(6)

        for (i in 0..5) {
            val reqItemId = i + 1
            val value = params.getValue("rateLimits[$reqItemId]")

            if (value.isEmpty()) {
                request.session().attribute(WebRouter.FLASH_MESSAGE, "<h4>Rate limits are invalid</h4>")

                return null
            }

            rateLimits[i] = value.toLong()
        }

        return rateLimits
    }

    private fun parseWarnActions(guild: Guild, params: Map<String, String>): List<WarnAction> {
        val warnActionsList = arrayListOf<WarnAction>()
        val isGuildPatron = CommandUtils.isGuildPatron(guild)
        val maxWarningActionCount = if(isGuildPatron) WarnAction.PATRON_MAX_ACTIONS else 1


        for (i in 1 until maxWarningActionCount + 1) {
            // TODO: check to see for missing warn actions
            if (!params.containsKey("warningAction$i")) {
                continue
            }

            if (!params.containsKey("tempDays$i") ||
                !params.containsKey("threshold$i")
            ) {
                halt(400, "Invalid warn action detected")
            }

            if (
                // Check for empty values (they should never be empty)
                params["warningAction$i"].isNullOrEmpty() ||
                params["tempDays$i"].isNullOrEmpty() ||
                params["threshold$i"].isNullOrEmpty()
            ) {
                halt(400, "One or more warn actions has empty values")
            }

            val action = WarnAction.Type.valueOf(params.getValue("warningAction$i"))
            val duration = params.getValue("tempDays$i").toInt()
            val threshold = params.getValue("threshold$i").toInt()

            warnActionsList.add(WarnAction(action, threshold, duration))
        }

        return warnActionsList
    }

    private fun Float.minMax(min: Float, max: Float): Float {
        // max returns the highest value and min returns the lowest value
        return min(max, max(min, this))
    }
}
