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

package ml.duncte123.skybot.adapters

import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import org.json.JSONObject
import java.lang.Exception

import ml.duncte123.skybot.utils.GuildSettingsUtils.replaceNewLines
import ml.duncte123.skybot.utils.GuildSettingsUtils.ratelimmitChecks
import ml.duncte123.skybot.utils.GuildSettingsUtils.toLong
import ml.duncte123.skybot.utils.GuildSettingsUtils.toBool

class WebDatabaseAdapter(private val variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {

        variables.database.run {
            try {

                val customCommands = arrayListOf<CustomCommand>()

                val array = variables.apis.getCustomCommands()

                array.forEach { c ->
                    val j = c as JSONObject

                    customCommands.add(CustomCommandImpl(
                        j.getString("invoke"),
                        j.getString("message"),
                        j.getLong("guildId")
                    ))
                }

                callback.invoke(customCommands)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.createCustomCommand(guildId, invoke, message)
            )
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.updateCustomCommand(guildId, invoke, message)
            )
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(variables.apis.deleteCustomCommand(guildId, invoke))
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        variables.database.run {
            try {
                val settings = arrayListOf<GuildSettings>()
                val array = variables.apis.getGuildSettings()

                array.forEach { c ->
                    val j = c as JSONObject

                    val setting = GuildSettings(j.getLong("guildId"))
                        .setEnableJoinMessage(toBool(j.getInt("enableJoinMessage")))
                        .setEnableSwearFilter(toBool(j.getInt("enableSwearFilter")))
                        .setCustomJoinMessage(replaceNewLines(j.getString("customWelcomeMessage")))
                        .setCustomPrefix(j.getString("prefix"))
                        .setLogChannel(toLong(j.optString("logChannelId")))
                        .setWelcomeLeaveChannel(toLong(j.optString("welcomeLeaveChannel")))
                        .setCustomLeaveMessage(replaceNewLines(j.getString("customLeaveMessage")))
                        .setAutoroleRole(toLong(j.optString("autoRole")))
                        .setServerDesc(replaceNewLines(j.optString("serverDesc", null)))
                        .setAnnounceTracks(toBool(j.getInt("announceNextTrack")))
                        .setAutoDeHoist(toBool(j.getInt("autoDeHoist")))
                        .setFilterInvites(toBool(j.getInt("filterInvites")))
                        .setEnableSpamFilter(toBool(j.getInt("spamFilterState")))
                        .setMuteRoleId(toLong(j.optString("muteRoleId")))
                        .setRatelimits(ratelimmitChecks(j.getString("ratelimits")))
                        .setKickState(toBool(j.getInt("kickInsteadState")))

                    settings.add(setting)
                }

                callback.invoke(settings)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.registerNewGuild(guildSettings)
            )
        }
    }
}
