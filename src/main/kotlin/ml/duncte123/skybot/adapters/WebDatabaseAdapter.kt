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

package ml.duncte123.skybot.adapters

import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.map.hash.TLongLongHashMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.Warning
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.GuildSettingsUtils.*
import org.json.JSONObject
import java.sql.Date

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebDatabaseAdapter(variables: Variables) : DatabaseAdapter(variables) {

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
                        j.getLong("guildId"),
                        j.getBoolean("autoresponse")
                    ))
                }

                callback.invoke(customCommands)
            } catch (e: Exception) {
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
                    val json = c as JSONObject

                    val setting = GuildSettings(json.getLong("guildId"))
                        .setEnableJoinMessage(toBool(json.getInt("enableJoinMessage")))
                        .setEnableSwearFilter(toBool(json.getInt("enableSwearFilter")))
                        .setCustomJoinMessage(replaceNewLines(json.getString("customWelcomeMessage")))
                        .setCustomPrefix(json.getString("prefix"))
                        .setLogChannel(toLong(json.optString("logChannelId")))
                        .setWelcomeLeaveChannel(toLong(json.optString("welcomeLeaveChannel")))
                        .setCustomLeaveMessage(replaceNewLines(json.getString("customLeaveMessage")))
                        .setAutoroleRole(toLong(json.optString("autoRole")))
                        .setServerDesc(replaceNewLines(json.optString("serverDesc", null)))
                        .setAnnounceTracks(toBool(json.getInt("announceNextTrack")))
                        .setAutoDeHoist(toBool(json.getInt("autoDeHoist")))
                        .setFilterInvites(toBool(json.getInt("filterInvites")))
                        .setEnableSpamFilter(toBool(json.getInt("spamFilterState")))
                        .setMuteRoleId(toLong(json.optString("muteRoleId")))
                        .setRatelimits(ratelimmitChecks(json.getString("ratelimits")))
                        .setKickState(toBool(json.getInt("kickInsteadState")))

                    settings.add(setting)
                }

                callback.invoke(settings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit) {
        variables.database.run {

            val item = variables.apis.getGuildSetting(guildId)

            val setting = GuildSettings(item.getLong("guildId"))
                .setEnableJoinMessage(toBool(item.getInt("enableJoinMessage")))
                .setEnableSwearFilter(toBool(item.getInt("enableSwearFilter")))
                .setCustomJoinMessage(replaceNewLines(item.getString("customWelcomeMessage")))
                .setCustomPrefix(item.getString("prefix"))
                .setLogChannel(toLong(item.optString("logChannelId")))
                .setWelcomeLeaveChannel(toLong(item.optString("welcomeLeaveChannel")))
                .setCustomLeaveMessage(replaceNewLines(item.getString("customLeaveMessage")))
                .setAutoroleRole(toLong(item.optString("autoRole")))
                .setServerDesc(replaceNewLines(item.optString("serverDesc", null)))
                .setAnnounceTracks(toBool(item.getInt("announceNextTrack")))
                .setAutoDeHoist(toBool(item.getInt("autoDeHoist")))
                .setFilterInvites(toBool(item.getInt("filterInvites")))
                .setEnableSpamFilter(toBool(item.getInt("spamFilterState")))
                .setMuteRoleId(toLong(item.optString("muteRoleId")))
                .setRatelimits(ratelimmitChecks(item.getString("ratelimits")))
                .setKickState(toBool(item.getInt("kickInsteadState")))

            callback.invoke(setting)
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.updateGuildSettings(guildSettings)
            )
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.registerNewGuildSettings(guildSettings)
            )
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        variables.database.run {
            val map = TLongIntHashMap()

            variables.apis.loadEmbedSettings().forEach {
                val json = it as JSONObject

                map.put(json.getLong("guild_id"), json.getInt("embed_color"))
            }

            callback.invoke(map)
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        variables.database.run {
            variables.apis.updateOrCreateEmbedColor(guildId, color)
        }
    }

    override fun loadOneGuildPatrons(callback: (TLongLongMap) -> Unit) {
        variables.database.run {
            val map = TLongLongHashMap()

            variables.apis.loadOneGuildPatrons().forEach {
                val json = it as JSONObject

                map.put(json.getLong("user_id"), json.getLong("guild_id"))
            }

            callback.invoke(map)
        }
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        variables.database.run {
            val status = variables.apis.updateOrCreateOneGuildPatron(userId, guildId)

            if (status) {
                callback.invoke(userId, guildId)
            }
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        variables.database.run {
            val map = TLongLongHashMap()

            variables.apis.getOneGuildPatron(userId).forEach {
                val json = it as JSONObject

                map.put(json.getLong("user_id"), json.getLong("guild_id"))
            }

            callback.invoke(map)
        }
    }

    override fun removeOneGuildPatron(userId: Long) {
        variables.database.run {
            variables.apis.removeOneGuildPatron(userId)
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        variables.database.run {
            val json = JSONObject()
                .put("modUserID", modId.toString())
                .put("Username", userName)
                .put("discriminator", userDiscriminator)
                .put("userId", userId.toString())
                .put("guildId", guildId.toString())
                .put("unban_date", unbanDate)

            variables.apis.createBan(json)
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        variables.database.run {
            variables.apis.createWarning(modId, userId, guildId, reason)
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long) {
        variables.database.run {
            val json = JSONObject()
                .put("mod_id", modId.toString())
                .put("user_id", userId.toString())
                .put("user_tag", userTag)
                .put("guild_id", guildId.toString())
                .put("unmute_date", unmuteDate)

            variables.apis.createMute(json)
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        variables.database.run {
            val data = variables.apis.getWarningsForUser(userId, guildId)
            val items = arrayListOf<Warning>()

            data.forEach {
                val json = it as JSONObject

                items.add(Warning(
                    json.getInt("id"),
                    Date.valueOf(json.getString("warn_date")),
                    Date.valueOf(json.getString("expire_date")),
                    json.getString("mod_id"),
                    json.getString("reason"),
                    json.getString("guild_id")
                ))
            }

            callback.invoke(items)
        }
    }

    override fun purgeBans(ids: List<Int>) {
        variables.database.run {
            variables.apis.purgeBans(ids)
        }
    }

    override fun purgeMutes(ids: List<Int>) {
        variables.database.run {
            variables.apis.purgeMutes(ids)
        }
    }

    override fun getExpiredBansAndMutes(callback: (Pair<List<Ban>, List<Mute>>) -> Unit) {
        variables.database.run {
            val bans = arrayListOf<Ban>()
            val mutes = arrayListOf<Mute>()

            val storedData = variables.apis.getExpiredBansAndMutes()
            val storedBans = storedData.getJSONArray("bans")
            val storedMutes = storedData.getJSONArray("mutes")

            storedBans.forEach {
                val json = it as JSONObject

                bans.add(Ban(
                    json.getInt("id"),
                    json.getString("modUserId"),
                    json.getString("userId"),
                    json.getString("Username"),
                    json.getString("discriminator"),
                    json.getString("guildId")
                ))
            }

            storedMutes.forEach {
                val json = it as JSONObject

                mutes.add(Mute(
                    json.getInt("id"),
                    json.getString("mod_id"),
                    json.getString("user_id"),
                    json.getString("user_tag"),
                    json.getString("guild_id")
                ))
            }

            callback.invoke(Pair(bans, mutes))
        }
    }
}
