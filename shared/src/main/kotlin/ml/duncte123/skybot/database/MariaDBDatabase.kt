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
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CommandResult
import ml.duncte123.skybot.objects.command.CustomCommand
import java.sql.Connection
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

class MariaDBDatabase(jdbcURI: String, ohShitFn: (Int, Int) -> Unit = { _, _ -> }) : AbstractDatabase(2, ohShitFn) {
    private val ds: HikariDataSource
    private val connection: Connection
        get() {
            return this.ds.connection
        }

    init {
        val config = HikariConfig()

        config.jdbcUrl = jdbcURI

        this.ds = HikariDataSource(config)
    }

    override fun getCustomCommands(): CompletableFuture<List<CustomCommand>> {
        TODO("Not yet implemented")
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String): CompletableFuture<CommandResult> {
        TODO("Not yet implemented")
    }

    override fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean
    ): CompletableFuture<CommandResult> {
        TODO("Not yet implemented")
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getGuildSettings(): CompletableFuture<List<GuildSetting>> {
        TODO("Not yet implemented")
    }

    override fun loadGuildSetting(guildId: Long): CompletableFuture<GuildSetting?> {
        TODO("Not yet implemented")
    }

    override fun deleteGuildSetting(guildId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun purgeGuildSettings(guildIds: List<Long>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateGuildSetting(guildSettings: GuildSetting): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun registerNewGuild(guildSettings: GuildSetting): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun addWordToBlacklist(guildId: Long, word: String): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun clearBlacklist(guildId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun loadAllPatrons(): CompletableFuture<AllPatronsData> {
        TODO("Not yet implemented")
    }

    override fun removePatron(userId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun createOrUpdatePatron(patron: Patron): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long): CompletableFuture<Pair<Long, Long>> {
        TODO("Not yet implemented")
    }

    override fun getOneGuildPatron(userId: Long): CompletableFuture<Long?> {
        TODO("Not yet implemented")
    }

    override fun createBan(modId: Long, userId: Long, unbanDate: String, guildId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun createMute(
        modId: Long,
        userId: Long,
        userTag: String,
        unmuteDate: String,
        guildId: Long
    ): CompletableFuture<Mute?> {
        TODO("Not yet implemented")
    }

    override fun getWarningsForUser(userId: Long, guildId: Long): CompletableFuture<List<Warning>> {
        TODO("Not yet implemented")
    }

    override fun getWarningCountForUser(userId: Long, guildId: Long): CompletableFuture<Int> {
        TODO("Not yet implemented")
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long): CompletableFuture<Warning?> {
        TODO("Not yet implemented")
    }

    override fun getExpiredBansAndMutes(): CompletableFuture<Pair<List<Ban>, List<Mute>>> {
        TODO("Not yet implemented")
    }

    override fun purgeBans(ids: List<Int>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun purgeMutes(ids: List<Int>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun createBanBypass(guildId: Long, userId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun getBanBypass(guildId: Long, userId: Long): CompletableFuture<BanBypas?> {
        TODO("Not yet implemented")
    }

    override fun deleteBanBypass(banBypass: BanBypas): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun getVcAutoRoles(): CompletableFuture<List<VcAutoRole>> {
        TODO("Not yet implemented")
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun removeVcAutoRole(voiceChannelId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun removeVcAutoRoleForGuild(guildId: Long): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun loadTags(): CompletableFuture<List<Tag>> {
        TODO("Not yet implemented")
    }

    override fun createTag(tag: Tag): CompletableFuture<Pair<Boolean, String>> {
        TODO("Not yet implemented")
    }

    override fun deleteTag(tag: Tag): CompletableFuture<Pair<Boolean, String>> {
        TODO("Not yet implemented")
    }

    override fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: OffsetDateTime,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean
    ): CompletableFuture<Pair<Boolean, Int>> {
        TODO("Not yet implemented")
    }

    override fun removeReminder(reminderId: Int, userId: Long): CompletableFuture<Boolean> {
        TODO("Not yet implemented")
    }

    override fun showReminder(reminderId: Int, userId: Long): CompletableFuture<Reminder?> {
        TODO("Not yet implemented")
    }

    override fun listReminders(userId: Long): CompletableFuture<List<Reminder>> {
        TODO("Not yet implemented")
    }

    override fun getExpiredReminders(): CompletableFuture<List<Reminder>> {
        TODO("Not yet implemented")
    }

    override fun purgeReminders(ids: List<Int>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }
}
