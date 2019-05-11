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

import com.fasterxml.jackson.core.type.TypeReference
import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.map.hash.TLongLongHashMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.VcAutoRole
import ml.duncte123.skybot.objects.api.Warning
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import java.sql.Date

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebDatabaseAdapter(variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        variables.database.run {
            try {
                val array = variables.apis.getCustomCommands()
                val customCommands: List<CustomCommand> = variables.jackson.readValue(array.traverse(), object : TypeReference<List<CustomCommandImpl>>() {})

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

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.updateCustomCommand(guildId, invoke, message, autoresponse)
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
                val mapper = variables.jackson
                val array = variables.apis.getGuildSettings()
                val settings: List<GuildSettings> = mapper.readValue(array.traverse(), object : TypeReference<List<GuildSettings>>() {})

                callback.invoke(settings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit) {
        variables.database.run {
            val item = variables.apis.getGuildSetting(guildId)
            val setting = variables.jackson.readValue(item.traverse(), GuildSettings::class.java)

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

    override fun deleteGuildSetting(guildId: Long) {
        variables.database.run {
            variables.apis.deleteGuildSetting(guildId)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            callback.invoke(
                variables.apis.registerNewGuildSettings(guildSettings)
            )
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        variables.database.run {
            variables.apis.addWordToBlacklist(guildId, word)
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        variables.database.run {
            variables.apis.removeWordFromBlacklist(guildId, word)
        }
    }

    override fun clearBlacklist(guildId: Long) {
        variables.database.run {
            variables.apis.clearBlacklist(guildId)
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        variables.database.run {
            val map = TLongIntHashMap()

            variables.apis.loadEmbedSettings().forEach {
                map.put(it.get("guild_id").asLong(), it.get("embed_color").asInt())
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
                map.put(it.get("user_id").asLong(), it.get("guild_id").asLong())
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
                map.put(it.get("user_id").asLong(), it.get("guild_id").asLong())
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
            val json = variables.jackson.createObjectNode()
                .put("modUserId", modId.toString())
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
            val json = variables.jackson.createObjectNode()
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

            data.forEach { json ->
                items.add(Warning(
                    json.get("id").asInt(),
                    Date.valueOf(json.get("warn_date").asText()),
                    Date.valueOf(json.get("expire_date").asText()),
                    json.get("mod_id").asText(),
                    json.get("reason").asText(),
                    json.get("guild_id").asText()
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
            val mapper = variables.jackson

            val storedData = variables.apis.getExpiredBansAndMutes()
            val storedBans = storedData.get("bans")
            val storedMutes = storedData.get("mutes")

            val bans: List<Ban> = mapper.readValue(storedBans.traverse(), object : TypeReference<List<Ban>>() {})
            val mutes: List<Mute> = mapper.readValue(storedMutes.traverse(), object : TypeReference<List<Mute>>() {})

            callback.invoke(Pair(bans, mutes))
        }
    }

    override fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit) {
        variables.database.run {
            val mapper = variables.jackson

            val storedData = variables.apis.getVcAutoRoles()
            val converted: List<VcAutoRole> = mapper.readValue(storedData.traverse(), object : TypeReference<List<VcAutoRole>>() {})

            callback.invoke(converted)
        }
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        variables.database.run {
            variables.apis.setVcAutoRole(guildId, voiceChannelId, roleId)
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        variables.database.run {
            variables.apis.removeVcAutoRole(voiceChannelId)
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        variables.database.run {
            variables.apis.removeVcAutoRoleForGuild(guildId)
        }
    }
}
