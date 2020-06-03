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

import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import io.sentry.Sentry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.guild.GuildSettings
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
abstract class DatabaseAdapter {

    //////////////////
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
    abstract fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit)

    abstract fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit)

    abstract fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?)

    /////////////////
    // Guild settings

    abstract fun getGuildSettings(callback: (List<GuildSettings>) -> Unit)

    abstract fun loadGuildSetting(guildId: Long, callback: (GuildSettings?) -> Unit)

    abstract fun deleteGuildSetting(guildId: Long)

    abstract fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit)

    abstract fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit)

    abstract fun addWordToBlacklist(guildId: Long, word: String)

    abstract fun addWordsToBlacklist(guildId: Long, words: List<String>)

    abstract fun removeWordFromBlacklist(guildId: Long, word: String)

    abstract fun clearBlacklist(guildId: Long)

    /////////////////
    // Embed settings

    abstract fun loadEmbedSettings(callback: (TLongIntMap) -> Unit)

    abstract fun updateOrCreateEmbedColor(guildId: Long, color: Int)

    ///////////////
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

    /////////////
    // Moderation

    abstract fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long)

    abstract fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String)

    abstract fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long, callback: (Mute?) -> Unit)

    abstract fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit)

    abstract fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit)

    abstract fun getExpiredBansAndMutes(callback: (List<Ban>, List<Mute>) -> Unit)

    abstract fun purgeBans(ids: List<Int>)

    abstract fun purgeMutes(ids: List<Int>)

    ///////////////
    // VC auto role

    abstract fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit)

    abstract fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long)

    abstract fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long)

    abstract fun removeVcAutoRole(voiceChannelId: Long)

    abstract fun removeVcAutoRoleForGuild(guildId: Long)

    ///////
    // Tags

    abstract fun loadTags(callback: (List<Tag>) -> Unit)

    abstract fun createTag(tag: Tag, callback: (Boolean, String) -> Unit)

    abstract fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit)

    // Reminders

    fun createReminder(userId: Long, reminder: String, expireDate: Date, callback: (Boolean) -> Unit) {
        createReminder(userId, reminder, expireDate, 0, callback)
    }

    abstract fun createReminder(userId: Long, reminder: String, expireDate: Date, channelId: Long, callback: (Boolean) -> Unit)

    // user id for security, a user can only remove their own reminders
    abstract fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit)

    abstract fun showReminder(reminderId: Int, userId: Long, callback: (Reminder?) -> Unit)

    abstract fun listReminders(userId: Long, callback: (List<Reminder>) -> Unit)

    abstract fun purgeReminders(ids: List<Int>)

    /**
     * Important: Callback must not be called if the list is empty
     */
    abstract fun getExpiredReminders(callback: (List<Reminder>) -> Unit)

    protected fun runOnThread(r: () -> Unit) {
        runOnThread(r) {
            //
        }
    }

    // Cannot be an option callback due to it targeting the onFail param
    protected fun runOnThread(r: () -> Unit, onFail: (Throwable) -> Unit) {
        GlobalScope.launch {
            try {
                r.invoke()
            } catch (thr: Throwable) {
                Sentry.capture(thr)
                onFail.invoke(thr)
                thr.printStackTrace()
            }
        }
    }

    protected fun String.parseDate(): Date {
        val split = this.split("-").map(String::toInt)
        val cal = Calendar.getInstance()

            cal.set(split[0], split[1], split[2])

        return cal.time
    }
}
