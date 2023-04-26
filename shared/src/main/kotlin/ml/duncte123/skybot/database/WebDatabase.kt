/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.database

import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.settings.WarnAction
import com.dunctebot.models.utils.DateUtils
import com.dunctebot.models.utils.Utils
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CustomCommand
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

class WebDatabase(private val apis: DuncteApis, private val jackson: ObjectMapper, ohShitFn: (Int, Int) -> Unit = { _, _ -> }) : AbstractDatabase(2, ohShitFn) {

    override fun getCustomCommands() = runOnThread {
        val array = apis.getCustomCommands()

        jackson.readValue(array.traverse(), object : TypeReference<List<CustomCommand>>() {})
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String) = runOnThread {
        apis.createCustomCommand(guildId, invoke, message)
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean) = runOnThread {
        apis.updateCustomCommand(guildId, invoke, message, autoresponse)
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String) = runOnThread {
        apis.deleteCustomCommand(guildId, invoke)
    }

    override fun getGuildSettings() = runOnThread {
        val array = apis.getGuildSettings()

        jackson.readValue(array.traverse(), object : TypeReference<List<GuildSetting>>() {})
    }

    override fun loadGuildSetting(guildId: Long) = runOnThread {
        val item = apis.getGuildSetting(guildId) ?: return@runOnThread null

        jackson.readValue(item.traverse(), GuildSetting::class.java)
    }

    override fun updateGuildSetting(guildSettings: GuildSetting) = runOnThread {
        apis.updateGuildSettings(guildSettings)
    }

    override fun deleteGuildSetting(guildId: Long) = runOnThread {
        apis.deleteGuildSetting(guildId)
    }

    override fun purgeGuildSettings(guildIds: List<Long>) = runOnThread {
        apis.purgeGuildSettings(guildIds)
    }

    override fun registerNewGuild(guildSettings: GuildSetting) = runOnThread {
        apis.registerNewGuildSettings(guildSettings)
    }

    override fun addWordToBlacklist(guildId: Long, word: String) = runOnThread {
        apis.addWordToBlacklist(guildId, word)
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) = runOnThread {
        apis.addBatchToBlacklist(guildId, words)
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) = runOnThread {
        apis.removeWordFromBlacklist(guildId, word)
    }

    override fun clearBlacklist(guildId: Long) = runOnThread {
        apis.clearBlacklist(guildId)
    }

    @Deprecated("Stored in guild settings")
    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) = runOnThread {
        apis.updateOrCreateEmbedColor(guildId, color)
    }

    override fun loadAllPatrons() = runOnThread {
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

        return@runOnThread AllPatronsData(patrons, tagPatrons, oneGuildPatrons, guildPatrons)
    }

    override fun removePatron(userId: Long) = runOnThread {
        apis.deletePatron(userId)
    }

    override fun createOrUpdatePatron(patron: Patron) = runOnThread {
        apis.createOrUpdatePatron(patron)
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long) = runOnThread {
        apis.updateOrCreateOneGuildPatron(userId, guildId)

        return@runOnThread userId to guildId
    }

    override fun getOneGuildPatron(userId: Long) = runOnThread {
        val patron = apis.getOneGuildPatron(userId) ?: return@runOnThread null

        return@runOnThread patron["guild_id"].asLong()
    }

    override fun createBan(
        modId: Long,
        userId: Long,
        unbanDate: String,
        guildId: Long
    ) = runOnThread {
        val json = jackson.createObjectNode()
            .put("modUserId", modId.toString())
            .put("Username", "Deleted User")
            .put("discriminator", "0000")
            .put("userId", userId.toString())
            .put("guildId", guildId.toString())
            .put("unban_date", unbanDate)

        apis.createBan(json)
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) = runOnThread {
        apis.createWarning(modId, userId, guildId, reason)
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long) = runOnThread {
        val json = jackson.createObjectNode()
            .put("mod_id", modId.toString())
            .put("user_id", userId.toString())
            .put("user_tag", userTag)
            .put("guild_id", guildId.toString())
            .put("unmute_date", unmuteDate)

        val muteData = apis.createMute(json)

        if (muteData.isEmpty) {
            return@runOnThread null
        }

        val mute: Mute = jackson.readValue(muteData.traverse(), Mute::class.java)

        return@runOnThread mute
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long) = runOnThread {
        val json = apis.removeLatestWarningForUser(userId, guildId)

        if (json == null) {
            return@runOnThread null
        }

        return@runOnThread Warning(
            json["id"].asInt(),
            json["warn_date"].asText(),
            json["mod_id"].asLong(),
            json["reason"].asText(),
            json["guild_id"].asLong()
        )
    }

    override fun getWarningsForUser(userId: Long, guildId: Long) = runOnThread {
        val data = apis.getWarningsForUser(userId, guildId)
        val items = arrayListOf<Warning>()

        val regex = "\\s+".toRegex()
        data.forEach { json ->
            items.add(
                Warning(
                    json["id"].asInt(),
                    json["warn_date"].asText().split(regex)[0],
                    json["mod_id"].asLong(),
                    json["reason"].asText(),
                    json["guild_id"].asLong()
                )
            )
        }

        return@runOnThread items.toList()
    }

    override fun getWarningCountForUser(userId: Long, guildId: Long) = runOnThread {
        apis.getWarningCountForUser(userId, guildId)
    }

    override fun getExpiredBansAndMutes(): CompletableFuture<Pair<List<Ban>, List<Mute>>> = runOnThread {
        TODO("Not supported")
    }

    override fun purgeBans(ids: List<Int>) = runOnThread { apis.purgeBans(ids) }

    override fun purgeMutes(ids: List<Int>) = runOnThread { apis.purgeMutes(ids) }

    override fun getVcAutoRoles() = runOnThread {
        val storedData = apis.getVcAutoRoles()
        val converted = arrayListOf<VcAutoRole>()

        for (item in storedData) {
            converted.add(
                VcAutoRole(
                    item["guild_id"].asLong(),
                    item["voice_channel_id"].asLong(),
                    item["role_id"].asLong()
                )
            )
        }

        return@runOnThread converted.toList()
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) = runOnThread {
        apis.setVcAutoRole(guildId, voiceChannelId, roleId)
    }

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) = runOnThread {
        apis.setVcAutoRoleBatch(guildId, voiceChannelIds, roleId)
    }

    override fun removeVcAutoRole(voiceChannelId: Long) = runOnThread {
        apis.removeVcAutoRole(voiceChannelId)
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) = runOnThread {
        apis.removeVcAutoRoleForGuild(guildId)
    }

    override fun loadTags() = runOnThread {
        val allTags = apis.getAllTags()

        jackson.readValue(allTags.traverse(), object : TypeReference<List<Tag>>() {})
    }

    override fun createTag(tag: Tag) = runOnThread {
        val json = jackson.valueToTree(tag) as ObjectNode
        json.put("owner_id", json["owner_id"].asText())

        val response = apis.createTag(json)

        return@runOnThread response.first to response.second
    }

    override fun deleteTag(tag: Tag) = runOnThread {
        val response = apis.deleteTag(tag.name)

        return@runOnThread response.first to response.second
    }

    override fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: OffsetDateTime,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean
    ) = runOnThread {
        val date = DateUtils.getDatabaseDateFormat(expireDate)
        val (res, reminderId) = apis.createReminder(
            userId,
            reminder,
            date,
            channelId,
            messageId,
            guildId,
            inChannel
        )

        return@runOnThread res to reminderId
    }

    override fun removeReminder(reminderId: Int, userId: Long) = runOnThread {
        apis.deleteReminder(userId, reminderId)
    }

    override fun showReminder(reminderId: Int, userId: Long) = runOnThread {
        val reminderJson = apis.showReminder(userId, reminderId)
        val reminder: Reminder? = jackson.readValue(reminderJson.traverse(), Reminder::class.java)

        return@runOnThread reminder
    }

    override fun listReminders(userId: Long) = runOnThread {
        val remindersJson = apis.listReminders(userId)
        val reminders = jackson.readValue(remindersJson.traverse(), object : TypeReference<List<Reminder>>() {})

        return@runOnThread reminders
    }

    override fun getExpiredReminders(): CompletableFuture<List<Reminder>> = runOnThread {
        TODO("Not supported :(")
    }

    override fun purgeReminders(ids: List<Int>) = runOnThread { apis.purgeReminders(ids) }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>) = runOnThread {
        apis.setWarnActions(guildId, actions)
    }

    override fun createBanBypass(guildId: Long, userId: Long) = runOnThread {
        apis.createBanBypass(guildId, userId)
    }

    override fun getBanBypass(guildId: Long, userId: Long) = runOnThread {
        val bypassJson = apis.getBanBypass(guildId, userId)
        val bypass: BanBypas? = jackson.readValue(bypassJson.traverse(), BanBypas::class.java)

        return@runOnThread bypass
    }

    override fun deleteBanBypass(banBypass: BanBypas) = runOnThread {
        apis.deleteBanBypass(banBypass.guildId, banBypass.userId)
        return@runOnThread
    }

    override fun purgeExpiredWarnings(): CompletableFuture<Unit> {
        TODO("Not Supported")
    }

    override fun close() {
        // Nothing to close
    }
}
