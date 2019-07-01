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
import com.fasterxml.jackson.databind.node.ObjectNode
import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.map.hash.TLongLongHashMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebDatabaseAdapter(variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        runOnThread {
            val array = variables.apis.getCustomCommands()
            val customCommands: List<CustomCommand> = variables.jackson.readValue(array.traverse(), object : TypeReference<List<CustomCommandImpl>>() {})

            callback.invoke(customCommands)
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            callback.invoke(
                variables.apis.createCustomCommand(guildId, invoke, message)
            )
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            callback.invoke(
                variables.apis.updateCustomCommand(guildId, invoke, message, autoresponse)
            )
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?) {
        runOnThread {
            callback.invoke(variables.apis.deleteCustomCommand(guildId, invoke))
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        runOnThread {
            val mapper = variables.jackson
            val array = variables.apis.getGuildSettings()
            val settings: List<GuildSettings> = mapper.readValue(array.traverse(), object : TypeReference<List<GuildSettings>>() {})

            callback.invoke(settings)
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings?) -> Unit) {
        runOnThread {
            val item = variables.apis.getGuildSetting(guildId)

            if (item == null) {
                callback.invoke(null)
                return@runOnThread
            }

            val setting = variables.jackson.readValue(item.traverse(), GuildSettings::class.java)

            callback.invoke(setting)
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            callback.invoke(
                variables.apis.updateGuildSettings(guildSettings)
            )
        }
    }

    override fun deleteGuildSetting(guildId: Long) {
        runOnThread {
            variables.apis.deleteGuildSetting(guildId)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            callback.invoke(
                variables.apis.registerNewGuildSettings(guildSettings)
            )
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        runOnThread {
            variables.apis.addWordToBlacklist(guildId, word)
        }
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) {
        runOnThread {
            variables.apis.addBatchToBlacklist(guildId, words)
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        runOnThread {
            variables.apis.removeWordFromBlacklist(guildId, word)
        }
    }

    override fun clearBlacklist(guildId: Long) {
        runOnThread {
            variables.apis.clearBlacklist(guildId)
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        runOnThread {
            val map = TLongIntHashMap()

            variables.apis.loadEmbedSettings().forEach {
                map.put(it.get("guild_id").asLong(), it.get("embed_color").asInt())
            }

            callback.invoke(map)
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        runOnThread {
            variables.apis.updateOrCreateEmbedColor(guildId, color)
        }
    }

    override fun loadOneGuildPatrons(callback: (TLongLongMap) -> Unit) {
        runOnThread {
            val map = TLongLongHashMap()

            variables.apis.loadOneGuildPatrons().forEach {
                map.put(it.get("user_id").asLong(), it.get("guild_id").asLong())
            }

            callback.invoke(map)
        }
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        runOnThread {
            val status = variables.apis.updateOrCreateOneGuildPatron(userId, guildId)

            if (status) {
                callback.invoke(userId, guildId)
            }
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        runOnThread {
            val map = TLongLongHashMap()

            variables.apis.getOneGuildPatron(userId).forEach {
                map.put(it.get("user_id").asLong(), it.get("guild_id").asLong())
            }

            callback.invoke(map)
        }
    }

    override fun removeOneGuildPatron(userId: Long) {
        runOnThread {
            variables.apis.removeOneGuildPatron(userId)
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        runOnThread {
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
        runOnThread {
            variables.apis.createWarning(modId, userId, guildId, reason)
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long) {
        runOnThread {
            val json = variables.jackson.createObjectNode()
                .put("mod_id", modId.toString())
                .put("user_id", userId.toString())
                .put("user_tag", userTag)
                .put("guild_id", guildId.toString())
                .put("unmute_date", unmuteDate)

            variables.apis.createMute(json)
        }
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit) {
        runOnThread {
            val json = variables.apis.removeLatestWarningForUser(userId, guildId)

            if (json == null) {
                callback.invoke(null)

                return@runOnThread
            }

            callback.invoke(Warning(
                json.get("id").asInt(),
                json.get("mod_id").asText(),
                json.get("reason").asText(),
                json.get("guild_id").asText()
            ))
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        runOnThread {
            val data = variables.apis.getWarningsForUser(userId, guildId)
            val items = arrayListOf<Warning>()

            data.forEach { json ->
                items.add(Warning(
                    json.get("id").asInt(),
                    json.get("mod_id").asText(),
                    json.get("reason").asText(),
                    json.get("guild_id").asText()
                ))
            }

            callback.invoke(items)
        }
    }

    override fun purgeBans(ids: List<Int>) {
        runOnThread {
            variables.apis.purgeBans(ids)
        }
    }

    override fun purgeMutes(ids: List<Int>) {
        runOnThread {
            variables.apis.purgeMutes(ids)
        }
    }

    override fun getExpiredBansAndMutes(callback: (Pair<List<Ban>, List<Mute>>) -> Unit) {
        runOnThread {
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
        runOnThread {
            val storedData = variables.apis.getVcAutoRoles()
            val converted = arrayListOf<VcAutoRole>()

            for (item in storedData) {
                converted.add(VcAutoRole(
                    item.get("guild_id").asLong(),
                    item.get("voice_channel_id").asLong(),
                    item.get("role_id").asLong()
                ))
            }

            callback.invoke(converted)
        }
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        runOnThread {
            variables.apis.setVcAutoRole(guildId, voiceChannelId, roleId)
        }
    }

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) {
        runOnThread {
            variables.apis.setVcAutoRoleBatch(guildId, voiceChannelIds, roleId)
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        runOnThread {
            variables.apis.removeVcAutoRole(voiceChannelId)
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        runOnThread {
            variables.apis.removeVcAutoRoleForGuild(guildId)
        }
    }

    override fun loadTags(callback: (List<Tag>) -> Unit) {
        runOnThread {
            val mapper = variables.jackson
            val allTags = variables.apis.getAllTags()

            callback.invoke(
                mapper.readValue<List<Tag>>(allTags.toString(), object : TypeReference<List<Tag>>() {})
            )
        }
    }

    override fun createTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            val json = variables.jackson.valueToTree(tag) as ObjectNode
            json.put("owner_id", json.get("owner_id").asText())

            val response = variables.apis.createTag(json)

            callback.invoke(response.first, response.second)
        }
    }

    override fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            val response = variables.apis.deleteTag(tag.name)

            callback.invoke(response.first, response.second)
        }
    }

    override fun createReminder(userId: Long, reminder: String, expireDate: Date, channelId: Long, callback: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun purgeReminders(ids: List<Int>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getExpiredReminders(callback: (List<Reminder>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
