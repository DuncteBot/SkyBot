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

package ml.duncte123.skybot.adapters

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.map.hash.TLongLongHashMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.objects.guild.WarnAction
import ml.duncte123.skybot.utils.AirUtils
import java.time.Instant

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WebDatabaseAdapter(private val apis: DuncteApis, private val jackson: ObjectMapper) : DatabaseAdapter() {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        runOnThread {
            val array = apis.getCustomCommands()
            val customCommands: List<CustomCommand> = jackson.readValue(array.traverse(), object : TypeReference<List<CustomCommandImpl>>() {})

            callback(customCommands)
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            callback(
                apis.createCustomCommand(guildId, invoke, message)
            )
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            callback(
                apis.updateCustomCommand(guildId, invoke, message, autoresponse)
            )
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?) {
        runOnThread {
            callback(apis.deleteCustomCommand(guildId, invoke))
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        runOnThread {
            val array = apis.getGuildSettings()
            val settings: List<GuildSettings> = jackson.readValue(array.traverse(), object : TypeReference<List<GuildSettings>>() {})

            callback(settings)
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings?) -> Unit) {
        runOnThread {
            val item = apis.getGuildSetting(guildId)

            if (item == null) {
                callback(null)
                return@runOnThread
            }

            val setting = jackson.readValue(item.traverse(), GuildSettings::class.java)

            callback(setting)
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            callback(
                apis.updateGuildSettings(guildSettings)
            )
        }
    }

    override fun deleteGuildSetting(guildId: Long) {
        runOnThread {
            apis.deleteGuildSetting(guildId)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            callback(
                apis.registerNewGuildSettings(guildSettings)
            )
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        runOnThread {
            apis.addWordToBlacklist(guildId, word)
        }
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) {
        runOnThread {
            apis.addBatchToBlacklist(guildId, words)
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        runOnThread {
            apis.removeWordFromBlacklist(guildId, word)
        }
    }

    override fun clearBlacklist(guildId: Long) {
        runOnThread {
            apis.clearBlacklist(guildId)
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        runOnThread {
            val map = TLongIntHashMap()

            apis.loadEmbedSettings().forEach {
                map.put(it["guild_id"].asLong(), it["embed_color"].asInt())
            }

            callback(map)
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        runOnThread {
            apis.updateOrCreateEmbedColor(guildId, color)
        }
    }

    override fun loadAllPatrons(callback: (AllPatronsData) -> Unit) {
        runOnThread {
            val patrons = arrayListOf<Patron>()
            val tagPatrons = arrayListOf<Patron>()
            val oneGuildPatrons = arrayListOf<Patron>()
            val guildPatrons = arrayListOf<Patron>()

            apis.loadAllPatrons()
                .map { jackson.readValue(it.traverse(), Patron::class.java) }
                .forEach { patron ->
                when (patron.type) {
                    Patron.Type.NORMAL -> patrons.add(patron)
                    Patron.Type.TAG -> tagPatrons.add(patron)
                    Patron.Type.ONE_GUILD -> oneGuildPatrons.add(patron)
                    Patron.Type.ALL_GUILD -> guildPatrons.add(patron)
                }
            }

            callback(AllPatronsData(patrons, tagPatrons, oneGuildPatrons, guildPatrons))
        }
    }

    override fun removePatron(userId: Long) {
        runOnThread {
            apis.deletePatron(userId)
        }
    }

    override fun createOrUpdatePatron(patron: Patron) {
        runOnThread {
            apis.createOrUpdatePatron(patron)
        }
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        runOnThread {
            val status = apis.updateOrCreateOneGuildPatron(userId, guildId)

            if (status) {
                callback(userId, guildId)
            }
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        runOnThread {
            val map = TLongLongHashMap()
            val patron = apis.getOneGuildPatron(userId) ?: return@runOnThread

            map.put(patron["user_id"].asLong(), patron["guild_id"].asLong())

            callback(map)
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        runOnThread {
            val json = jackson.createObjectNode()
                .put("modUserId", modId.toString())
                .put("Username", userName)
                .put("discriminator", userDiscriminator)
                .put("userId", userId.toString())
                .put("guildId", guildId.toString())
                .put("unban_date", unbanDate)

            apis.createBan(json)
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        runOnThread {
            apis.createWarning(modId, userId, guildId, reason)
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long, callback: (Mute?) -> Unit) {
        runOnThread {
            val json = jackson.createObjectNode()
                .put("mod_id", modId.toString())
                .put("user_id", userId.toString())
                .put("user_tag", userTag)
                .put("guild_id", guildId.toString())
                .put("unmute_date", unmuteDate)

            val muteData = apis.createMute(json)

            if (muteData.isEmpty) {
                callback(null)
                return@runOnThread
            }

            val mute: Mute = jackson.readValue(muteData.traverse(), Mute::class.java)

            callback(mute)
        }
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit) {
        runOnThread {
            val json = apis.removeLatestWarningForUser(userId, guildId)

            if (json == null) {
                callback(null)

                return@runOnThread
            }

            callback(Warning(
                json["id"].asInt(),
                json["warn_date"].asText(),
                json["mod_id"].asText(),
                json["reason"].asText(),
                json["guild_id"].asText()
            ))
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        runOnThread {
            val data = apis.getWarningsForUser(userId, guildId)
            val items = arrayListOf<Warning>()

            val regex = "\\s+".toRegex()
            data.forEach { json ->
                items.add(Warning(
                    json["id"].asInt(),
                    json["warn_date"].asText().split(regex)[0],
                    json["mod_id"].asText(),
                    json["reason"].asText(),
                    json["guild_id"].asText()
                ))
            }

            callback(items)
        }
    }

    override fun getWarningCountForUser(userId: Long, guildId: Long, callback: (Int) -> Unit) {
        runOnThread {
            callback(
                apis.getWarningCountForUser(userId, guildId)
            )
        }
    }

    override fun purgeBans(ids: List<Int>) {
        runOnThread {
            apis.purgeBans(ids)
        }
    }

    override fun purgeMutes(ids: List<Int>) {
        runOnThread {
            apis.purgeMutes(ids)
        }
    }

    override fun getExpiredBansAndMutes(callback: (List<Ban>, List<Mute>) -> Unit) {
        throw UnsupportedOperationException("Not used anymore")
    }

    override fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit) {
        runOnThread {
            val storedData = apis.getVcAutoRoles()
            val converted = arrayListOf<VcAutoRole>()

            for (item in storedData) {
                converted.add(VcAutoRole(
                    item["guild_id"].asLong(),
                    item["voice_channel_id"].asLong(),
                    item["role_id"].asLong()
                ))
            }

            callback(converted)
        }
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        runOnThread {
            apis.setVcAutoRole(guildId, voiceChannelId, roleId)
        }
    }

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) {
        runOnThread {
            apis.setVcAutoRoleBatch(guildId, voiceChannelIds, roleId)
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        runOnThread {
            apis.removeVcAutoRole(voiceChannelId)
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        runOnThread {
            apis.removeVcAutoRoleForGuild(guildId)
        }
    }

    override fun loadTags(callback: (List<Tag>) -> Unit) {
        runOnThread {
            val allTags = apis.getAllTags()

            callback(
                jackson.readValue(allTags.traverse(), object : TypeReference<List<Tag>>() {})
            )
        }
    }

    override fun createTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            val json = jackson.valueToTree(tag) as ObjectNode
            json.put("owner_id", json["owner_id"].asText())

            val response = apis.createTag(json)

            callback(response.first, response.second)
        }
    }

    override fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            val response = apis.deleteTag(tag.name)

            callback(response.first, response.second)
        }
    }

    override fun createReminder(userId: Long, reminder: String, expireDate: Instant, channelId: Long, callback: (Boolean, Int) -> Unit) {
        runOnThread {
            val date = AirUtils.getDatabaseDateFormat(expireDate)
            val (res, reminderId) = apis.createReminder(userId, reminder, date, channelId)

            callback(res, reminderId)
        }
    }

    override fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit) {
        runOnThread {
            callback(
                apis.deleteReminder(userId, reminderId)
            )
        }
    }

    override fun showReminder(reminderId: Int, userId: Long, callback: (Reminder?) -> Unit) {
        runOnThread {
            val reminderJson = apis.showReminder(userId, reminderId)
            val reminder: Reminder? = jackson.readValue(reminderJson.traverse(), Reminder::class.java)

            callback(reminder)
        }
    }

    override fun listReminders(userId: Long, callback: (List<Reminder>) -> Unit) {
        runOnThread {
            val remindersJson = apis.listReminders(userId)
            val reminders = jackson.readValue(remindersJson.traverse(), object : TypeReference<List<Reminder>>() {})

            callback(reminders)
        }
    }

    override fun purgeReminders(ids: List<Int>) {
        runOnThread {
            apis.purgeReminders(ids)
        }
    }

    override fun getExpiredReminders(callback: (List<Reminder>) -> Unit) {
        throw UnsupportedOperationException("Not used anymore")
    }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>) {
        runOnThread {
            apis.setWarnActions(guildId, actions)
        }
    }
}
