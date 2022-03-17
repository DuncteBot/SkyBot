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
import gnu.trove.map.TLongLongMap
import io.sentry.Sentry
import kotlinx.coroutines.*
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CustomCommand
import java.time.OffsetDateTime
import java.util.concurrent.Executors

abstract class AbstractDatabase(threads: Int = 2) {
    private val databaseThread = Executors.newFixedThreadPool(threads) {
        val t = Thread(it, "DatabaseThread")
        t.isDaemon = true
        t
    }

    // ////////////////
    // Custom commands

    abstract fun getCustomCommands(callback: (List<CustomCommand>) -> Unit)

    /**
     * Creates a custom command
     *
     * @param guildId
     *          the id of the guild
     *
     * @param invoke
     *          the invoke of the command
     *
     * @param message
     *          the action of the command
     *
     * @param callback
     *          the result of the action
     *          the boolean values in the tripple are:
     *             1. True when the command was added
     *             2. True when the guild already has a command with this invoke
     *             3. True when the guild reached the custom command limit
     */
    // TODO: replace tripple with CommandResult
    abstract fun createCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit
    )

    abstract fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean,
        callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit
    )

    abstract fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?)

    // ///////////////
    // Guild settings

    abstract fun getGuildSettings(callback: (List<GuildSetting>) -> Unit)

    abstract fun loadGuildSetting(guildId: Long, callback: (GuildSetting?) -> Unit)

    abstract fun deleteGuildSetting(guildId: Long)

    abstract fun purgeGuildSettings(guildIds: List<Long>)

    abstract fun updateGuildSetting(guildSettings: GuildSetting, callback: (Boolean) -> Unit)

    abstract fun registerNewGuild(guildSettings: GuildSetting, callback: (Boolean) -> Unit)

    abstract fun addWordToBlacklist(guildId: Long, word: String)

    abstract fun addWordsToBlacklist(guildId: Long, words: List<String>)

    abstract fun removeWordFromBlacklist(guildId: Long, word: String)

    abstract fun clearBlacklist(guildId: Long)

    // ///////////////
    // Embed settings

    @Deprecated("Stored in guild settings")
    abstract fun updateOrCreateEmbedColor(guildId: Long, color: Int)

    // /////////////
    // Patron stuff

    abstract fun loadAllPatrons(callback: (AllPatronsData) -> Unit)

    abstract fun removePatron(userId: Long)

    fun createOrUpdatePatron(type: Patron.Type, userId: Long, guildId: Long?) {
        val patron = Patron(type, userId, guildId)

        this.createOrUpdatePatron(patron)
    }

    abstract fun createOrUpdatePatron(patron: Patron)

    abstract fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit)

    abstract fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit)

    // ///////////
    // Moderation

    abstract fun createBan(
        modId: Long,
        userName: String,
        userDiscriminator: String,
        userId: Long,
        unbanDate: String,
        guildId: Long
    )

    abstract fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String, callback: () -> Unit = {})

    // callback is optional since we don't always need it
    abstract fun createMute(
        modId: Long,
        userId: Long,
        userTag: String,
        unmuteDate: String,
        guildId: Long,
        callback: (Mute?) -> Unit = {}
    )

    abstract fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit)

    abstract fun getWarningCountForUser(userId: Long, guildId: Long, callback: (Int) -> Unit)

    abstract fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit)

    @Deprecated("Switch to sync method", ReplaceWith("purgeBansSync(ids)"))
    fun purgeBans(ids: List<Int>) = runOnThread {
        this.purgeBansSync(ids)
    }

    abstract fun purgeBansSync(ids: List<Int>)

    @Deprecated("Switch to sync method", ReplaceWith("purgeMutesSync(ids)"))
    fun purgeMutes(ids: List<Int>) = runOnThread {
        this.purgeMutesSync(ids)
    }

    abstract fun purgeMutesSync(ids: List<Int>)

    abstract fun createBanBypass(guildId: Long, userId: Long)

    abstract fun getBanBypass(guildId: Long, userId: Long, callback: (BanBypas?) -> Unit)

    abstract fun deleteBanBypass(banBypass: BanBypas)

    // /////////////
    // VC auto role

    abstract fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit)

    abstract fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long)

    abstract fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long)

    abstract fun removeVcAutoRole(voiceChannelId: Long)

    abstract fun removeVcAutoRoleForGuild(guildId: Long)

    // /////
    // Tags

    abstract fun loadTags(callback: (List<Tag>) -> Unit)

    abstract fun createTag(tag: Tag, callback: (Boolean, String) -> Unit)

    abstract fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit)

    // Reminders

    abstract fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: OffsetDateTime,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean,
        callback: (Boolean, Int) -> Unit
    )

    fun removeReminder(reminder: Reminder, callback: (Boolean) -> Unit) {
        removeReminder(reminder.id, reminder.user_id, callback)
    }

    // user id for security, a user can only remove their own reminders
    abstract fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit)

    abstract fun showReminder(reminderId: Int, userId: Long, callback: (Reminder?) -> Unit)

    abstract fun listReminders(userId: Long, callback: (List<Reminder>) -> Unit)

    @Deprecated("Switch to sync method", ReplaceWith("purgeRemindersSync(ids)"))
    fun purgeReminders(ids: List<Int>) = runOnThread {
        this.purgeRemindersSync(ids)
    }

    abstract fun purgeRemindersSync(ids: List<Int>)

    abstract fun setWarnActions(guildId: Long, actions: List<WarnAction>)

    protected fun runOnThread(r: () -> Unit) {
        runOnThread(r) {
            //
        }
    }

    // Cannot be an option callback due to it targeting the onFail param
    /*protected fun runOnThread(r: () -> Unit, onFail: (Throwable) -> Unit = {}) {
        databaseThread.execute {
            try {
                r.invoke()
            } catch (thr: Throwable) {
                Sentry.captureException(thr)
                onFail.invoke(thr)
                thr.printStackTrace()
            }
        }
    }*/

    protected fun runOnThread(r: () -> Unit, onFail: (Throwable) -> Unit) = runBlocking {
        launch {
            try {
                r.invoke()
            } catch (thr: Throwable) {
                Sentry.captureException(thr)
                onFail.invoke(thr)
                thr.printStackTrace()
            }
        }
    }
}
