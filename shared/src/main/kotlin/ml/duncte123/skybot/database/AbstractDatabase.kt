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
import io.sentry.Sentry
// import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CommandResult
import ml.duncte123.skybot.objects.command.CustomCommand
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

abstract class AbstractDatabase(threads: Int = 2) {
    private val databaseThread = Executors.newFixedThreadPool(threads) {
        val t = Thread(it, "DatabaseThread")
        t.isDaemon = true
        t
    } as ThreadPoolExecutor
    private val databaseKiller = Executors.newScheduledThreadPool(threads) {
        val t = Thread(it, "Database-kill-Thread")
        t.isDaemon = true
        t
    }

    // Monitor the thread, hopefully this can help us
    init {
        databaseKiller.scheduleAtFixedRate(
            {
                // TODO: AAAAAAAA
                if (databaseThread.queue.size > 10) {
                    /*SkyBot.getInstance().shardManager
                        // #breaking-bots
                        .getTextChannelById(387881926691782657L)
                        ?.sendMessage("<@191231307290771456> Database thread queue is currently at ${databaseThread.queue.size} tasks! (active threads: ${databaseThread.activeCount})")
                        ?.queue()*/
                }
            }, 1L, 1L, TimeUnit.DAYS
        )
    }

    // ////////////////
    // Custom commands

    abstract fun getCustomCommands(): CompletableFuture<List<CustomCommand>>

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
     */
    abstract fun createCustomCommand(
        guildId: Long,
        invoke: String,
        message: String
    ): CompletableFuture<CommandResult>

    abstract fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean
    ): CompletableFuture<CommandResult>

    abstract fun deleteCustomCommand(guildId: Long, invoke: String): CompletableFuture<Boolean>

    // ///////////////
    // Guild settings

    abstract fun getGuildSettings(): CompletableFuture<List<GuildSetting>>

    abstract fun loadGuildSetting(guildId: Long): CompletableFuture<GuildSetting?>

    abstract fun deleteGuildSetting(guildId: Long): CompletableFuture<Unit>

    abstract fun purgeGuildSettings(guildIds: List<Long>): CompletableFuture<Unit>

    abstract fun updateGuildSetting(guildSettings: GuildSetting): CompletableFuture<Boolean>

    abstract fun registerNewGuild(guildSettings: GuildSetting): CompletableFuture<Boolean>

    abstract fun addWordToBlacklist(guildId: Long, word: String): CompletableFuture<Unit>

    abstract fun addWordsToBlacklist(guildId: Long, words: List<String>): CompletableFuture<Unit>

    abstract fun removeWordFromBlacklist(guildId: Long, word: String): CompletableFuture<Unit>

    abstract fun clearBlacklist(guildId: Long): CompletableFuture<Unit>

    // ///////////////
    // Embed settings

    @Deprecated("Stored in guild settings")
    abstract fun updateOrCreateEmbedColor(guildId: Long, color: Int): CompletableFuture<Unit>

    // /////////////
    // Patron stuff

    abstract fun loadAllPatrons(): CompletableFuture<AllPatronsData>

    abstract fun removePatron(userId: Long): CompletableFuture<Unit>

    fun createOrUpdatePatron(type: Patron.Type, userId: Long, guildId: Long?): CompletableFuture<Unit> {
        val patron = Patron(type, userId, guildId)

        return this.createOrUpdatePatron(patron)
    }

    abstract fun createOrUpdatePatron(patron: Patron): CompletableFuture<Unit>

    // why is this returning the two parameters?
    abstract fun addOneGuildPatrons(userId: Long, guildId: Long): CompletableFuture<Pair<Long, Long>>

    abstract fun getOneGuildPatron(userId: Long): CompletableFuture<Long?>

    // ///////////
    // Moderation

    // TODO: remove useless data
    abstract fun createBan(
        modId: Long,
        userId: Long,
        unbanDate: String,
        guildId: Long
    ): CompletableFuture<Unit>

    abstract fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String): CompletableFuture<Unit>

    abstract fun createMute(
        modId: Long,
        userId: Long,
        userTag: String,
        unmuteDate: String,
        guildId: Long
    ): CompletableFuture<Mute?>

    abstract fun getWarningsForUser(userId: Long, guildId: Long): CompletableFuture<List<Warning>>

    abstract fun getWarningCountForUser(userId: Long, guildId: Long): CompletableFuture<Int>

    abstract fun deleteLatestWarningForUser(userId: Long, guildId: Long): CompletableFuture<Warning?>

    abstract fun getExpiredBansAndMutes(): CompletableFuture<Pair<List<Ban>, List<Mute>>>

    abstract fun purgeBans(ids: List<Int>): CompletableFuture<Unit>

    abstract fun purgeMutes(ids: List<Int>): CompletableFuture<Unit>

    abstract fun createBanBypass(guildId: Long, userId: Long): CompletableFuture<Unit>

    abstract fun getBanBypass(guildId: Long, userId: Long): CompletableFuture<BanBypas?>

    abstract fun deleteBanBypass(banBypass: BanBypas): CompletableFuture<Unit>

    // /////////////
    // VC auto role

    abstract fun getVcAutoRoles(): CompletableFuture<List<VcAutoRole>>

    abstract fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long): CompletableFuture<Unit>

    abstract fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long): CompletableFuture<Unit>

    abstract fun removeVcAutoRole(voiceChannelId: Long): CompletableFuture<Unit>

    abstract fun removeVcAutoRoleForGuild(guildId: Long): CompletableFuture<Unit>

    // /////
    // Tags

    abstract fun loadTags(): CompletableFuture<List<Tag>>

    abstract fun createTag(tag: Tag): CompletableFuture<Pair<Boolean, String>>

    abstract fun deleteTag(tag: Tag): CompletableFuture<Pair<Boolean, String>>

    // Reminders

    abstract fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: OffsetDateTime,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean
    ): CompletableFuture<Pair<Boolean, Int>>

    fun removeReminder(reminder: Reminder) = removeReminder(reminder.id, reminder.user_id)

    // user id for security, a user can only remove their own reminders
    abstract fun removeReminder(reminderId: Int, userId: Long): CompletableFuture<Boolean>

    abstract fun showReminder(reminderId: Int, userId: Long): CompletableFuture<Reminder?>

    abstract fun listReminders(userId: Long): CompletableFuture<List<Reminder>>

    abstract fun getExpiredReminders(): CompletableFuture<List<Reminder>>

    abstract fun purgeReminders(ids: List<Int>): CompletableFuture<Unit>

    abstract fun setWarnActions(guildId: Long, actions: List<WarnAction>): CompletableFuture<Unit>

    protected fun <T> runOnThread(r: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()

        val runnableFuture = databaseThread.submit {
            try {
                val result = r.invoke()

                future.complete(result)
            } catch (thr: Throwable) {
                Sentry.captureException(thr)
                thr.printStackTrace()
                future.completeExceptionally(thr)
            }
        }

        // Kill the thread after 20 seconds, hopefully this works
        databaseKiller.schedule({
            runnableFuture.cancel(true)
        }, 20, TimeUnit.SECONDS)

        return future
    }
}
