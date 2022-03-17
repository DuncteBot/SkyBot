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
import com.dunctebot.models.utils.Utils.convertJ2S
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import gnu.trove.map.TLongLongMap
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import ml.duncte123.skybot.extensions.toGuildSetting
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CustomCommand
import java.sql.Connection
import java.time.OffsetDateTime

class PostreDatabase : AbstractDatabase() {
    private val ds: HikariDataSource
    private val connection: Connection
        get() { return this.ds.connection }

    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:postgresql://localhost:5432/skybot?user=skybot&password=password" // &ssl=true
        // config.addDataSourceProperty("logWriter", PrintWriter(System.out))

        this.ds = HikariDataSource(config)

        // could be useful?
        /*val dataTypeFactory = DataTypeFactory.getInstance()
        dataTypeFactory.register(Pos)*/

        this.connection.use { con ->
            Liquibase(
                "/dbchangelog.xml",
                ClassLoaderResourceAccessor(),
                JdbcConnection(con)
            ).use { lb ->
                lb.update(Contexts())
            }
        }
    }

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) = runOnThread {
        val customCommands = arrayListOf<CustomCommand>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM custom_commands").use { res ->
                    res.use {
                        while (res.next()) {
                            customCommands.add(
                                CustomCommand(
                                    res.getString("invoke"),
                                    res.getString("message"),
                                    res.getLong("guild_id"),
                                    res.getBoolean("auto_response")
                                )
                            )
                        }
                    }
                }
            }
        }

        callback(customCommands)
    }

    override fun createCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean,
        callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?) {
        TODO("Not yet implemented")
    }

    override fun getGuildSettings(callback: (List<GuildSetting>) -> Unit) = runOnThread {
        val settings = arrayListOf<GuildSetting>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM guild_settings").use { res ->
                    while (res.next()) {
                        val guildId = res.getLong("guild_id")
                        settings.add(
                            res.toGuildSetting()
                                // be smart and re-use the connection we already have
                                .setBlacklistedWords(getBlackListsForGuild(guildId, con))
                                .setWarnActions(getWarnActionsForGuild(guildId, con))
                        )
                    }
                }
            }
        }

        callback(settings)
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSetting?) -> Unit) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM guild_settings WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        callback.invoke(
                            res.toGuildSetting()
                                // be smart and re-use the connection we already have
                                .setBlacklistedWords(getBlackListsForGuild(guildId, con))
                                .setWarnActions(getWarnActionsForGuild(guildId, con))
                        )
                    } else {
                        callback.invoke(null)
                    }
                }
            }
        }
    }

    override fun deleteGuildSetting(guildId: Long) = purgeGuildSettings(listOf(guildId))

    override fun purgeGuildSettings(guildIds: List<Long>) = runOnThread {
        val queries = arrayOf(
            "DELETE FROM guild_settings WHERE guild_id IN",
            "DELETE FROM vc_autoroles WHERE guild_id IN",
            "DELETE FROM blacklisted_words WHERE guild_id IN",
            "DELETE FROM warn_actions WHERE guild_id IN",
            "DELETE FROM custom_commands WHERE guild_id IN",
        )
        val questions = guildIds.joinToString(", ") { "?" }

        this.connection.use { con ->
            queries.forEach { q ->
                con.prepareStatement("$q $questions") .use { smt ->
                    guildIds.forEachIndexed { index, id ->
                        smt.setLong(index + 1, id)
                    }

                    smt.execute()
                }
            }
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSetting, callback: (Boolean) -> Unit) = runOnThread {
        this.connection.use { con ->
            // TODO: remove server_description, discord has this feature now
            con.prepareStatement(
                """UPDATE guild_settings SET
                    |prefix = ?,
                    |auto_role_id = ?,
                    |embed_color = ?,
                    |voice_leave_timeout_seconds = ?,
                    |announce_track_enabled = ?,
                    |allow_all_to_stop = ?,
                    |server_description = ?,
                    |
                    |join_leave_channel_id = ?,
                    |join_message_enabled = ?,
                    |leave_message_enabled = ?,
                    |join_message = ?,
                    |leave_message = ?,
                    |
                    |log_channel_id = ?,
                    |mute_role_id = ?,
                    |swear_filter_enabled = ?,
                    |swear_filter_type = ?,
                    |swear_sensitivity = ?,
                    |
                    |auto_dehoist_enabled = ?,
                    |invite_filter_enabled = ?,
                    |spam_filter_state = ?,
                    |kick_instead_state = ?,
                    |ratelimits = ?,
                    |spam_threshold = ?,
                    |ban_young_account_enabled = ?,
                    |ban_young_account_threshold_days = ?,
                    |
                    |ban_logging_enabled = ?,
                    |unban_logging_enabled = ?,
                    |mute_logging_enabled = ?,
                    |warn_logging_enabled = ?,
                    |member_logging_enabled = ?,
                    |invite_logging_enabled = ?,
                    |message_logging_enabled = ?
                    |
                    |WHERE guild_id = ?
                """.trimMargin()
            ).use { smt ->
                smt.setString(1, guildSettings.customPrefix)
                smt.setLong(2, guildSettings.autoroleRole)
                smt.setInt(3, guildSettings.embedColor)
                smt.setInt(4, guildSettings.leaveTimeout)
                smt.setBoolean(4, guildSettings.isAnnounceTracks)
                smt.setBoolean(5, guildSettings.isAllowAllToStop)
                // TODO: remove, discord has this feature
                smt.setString(6, guildSettings.serverDesc)

                smt.setLong(7, guildSettings.welcomeLeaveChannel)
                smt.setBoolean(8, guildSettings.isEnableJoinMessage)
                smt.setBoolean(9, guildSettings.isEnableLeaveMessage)
                smt.setString(10, guildSettings.customJoinMessage)
                smt.setString(11, guildSettings.customLeaveMessage)

                smt.setLong(12, guildSettings.logChannel)
                smt.setLong(13, guildSettings.muteRoleId)
                smt.setBoolean(14, guildSettings.isEnableSwearFilter)
                smt.setString(15, guildSettings.filterType.type)
                smt.setFloat(16, guildSettings.aiSensitivity)

                smt.setBoolean(17, guildSettings.isAutoDeHoist)
                smt.setBoolean(18, guildSettings.isFilterInvites)
                smt.setBoolean(19, guildSettings.isEnableSpamFilter)
                smt.setBoolean(20, guildSettings.kickState)
                smt.setString(21, convertJ2S(guildSettings.ratelimits))
                smt.setInt(22, guildSettings.spamThreshold)
                smt.setBoolean(23, guildSettings.isYoungAccountBanEnabled)
                smt.setInt(24, guildSettings.youngAccountThreshold)

                // Logging :)
                smt.setBoolean(25, guildSettings.isBanLogging)
                smt.setBoolean(26, guildSettings.isUnbanLogging)
                smt.setBoolean(27, guildSettings.isMuteLogging)
                smt.setBoolean(28, guildSettings.isWarnLogging)
                smt.setBoolean(29, guildSettings.isMemberLogging)
                smt.setBoolean(30, guildSettings.isInviteLogging)
                smt.setBoolean(31, guildSettings.isMessageLogging)

                // What guild?
                smt.setLong(32, guildSettings.guildId)
            }
        }
    }

    override fun registerNewGuild(guildSettings: GuildSetting, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        TODO("Not yet implemented")
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) {
        TODO("Not yet implemented")
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        TODO("Not yet implemented")
    }

    override fun clearBlacklist(guildId: Long) {
        TODO("Not yet implemented")
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        TODO("Not yet implemented")
    }

    override fun loadAllPatrons(callback: (AllPatronsData) -> Unit) = runOnThread {
        val patrons = arrayListOf<Patron>()
        val tagPatrons = arrayListOf<Patron>()
        val oneGuildPatrons = arrayListOf<Patron>()
        val guildPatrons = arrayListOf<Patron>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM patrons").use { res ->
                    while (res.next()) {
                        val idRes = res.getLong("guild_id")
                        val guildId = if (idRes == 0L) null else idRes
                        val patron = Patron(
                            Patron.Type.valueOf(res.getString("type").uppercase()),
                            res.getLong("user_id"),
                            guildId
                        )

                        when (patron.type) {
                            Patron.Type.NORMAL -> patrons.add(patron)
                            Patron.Type.TAG -> tagPatrons.add(patron)
                            Patron.Type.ONE_GUILD -> oneGuildPatrons.add(patron)
                            Patron.Type.ALL_GUILD -> guildPatrons.add(patron)
                        }
                    }
                }
            }

            callback(AllPatronsData(patrons, tagPatrons, oneGuildPatrons, guildPatrons))
        }
    }

    override fun removePatron(userId: Long) {
        TODO("Not yet implemented")
    }

    override fun createOrUpdatePatron(patron: Patron) {
        TODO("Not yet implemented")
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createBan(
        modId: Long,
        userName: String,
        userDiscriminator: String,
        userId: Long,
        unbanDate: String,
        guildId: Long
    ) {
        TODO("Not yet implemented")
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String, callback: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createMute(
        modId: Long,
        userId: Long,
        userTag: String,
        unmuteDate: String,
        guildId: Long,
        callback: (Mute?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getWarningCountForUser(userId: Long, guildId: Long, callback: (Int) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun purgeBansSync(ids: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun purgeMutesSync(ids: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun createBanBypass(guildId: Long, userId: Long) {
        TODO("Not yet implemented")
    }

    override fun getBanBypass(guildId: Long, userId: Long, callback: (BanBypas?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun deleteBanBypass(banBypass: BanBypas) {
        TODO("Not yet implemented")
    }

    override fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit) = runOnThread {
        val roles = arrayListOf<VcAutoRole>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM vc_autoroles").use { res ->
                    while (res.next()) {
                        roles.add(
                            VcAutoRole(
                                res.getLong("guild_id"),
                                res.getLong("voice_channel_id"),
                                res.getLong("role_id")
                            )
                        )
                    }
                }
            }
        }

        callback(roles)
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) = setVcAutoRoleBatch(guildId, listOf(voiceChannelId), roleId)

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) {
        TODO("Not yet implemented")
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        TODO("Not yet implemented")
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        TODO("Not yet implemented")
    }

    override fun loadTags(callback: (List<Tag>) -> Unit) = runOnThread {
        val tags = arrayListOf<Tag>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM tags").use { res ->
                    while (res.next()) {
                        tags.add(
                            Tag(
                                res.getInt("id"),
                                res.getString("name"),
                                res.getString("content"),
                                res.getLong("owner_id")
                            )
                        )
                    }
                }
            }
        }

        callback(tags)
    }

    override fun createTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun createReminder(
        userId: Long,
        reminder: String,
        expireDate: OffsetDateTime,
        channelId: Long,
        messageId: Long,
        guildId: Long,
        inChannel: Boolean,
        callback: (Boolean, Int) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun showReminder(reminderId: Int, userId: Long, callback: (Reminder?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun listReminders(userId: Long, callback: (List<Reminder>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun purgeRemindersSync(ids: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>) {
        TODO("Not yet implemented")
    }

    private fun getBlackListsForGuild(guildId: Long, con: Connection): List<String> {
        val list = arrayListOf<String>()

        con.prepareStatement("SELECT word FROM blacklisted_words WHERE guild_id = ?").use { smt ->
            smt.setLong(1, guildId)

            smt.executeQuery().use { res ->
                while (res.next()) {
                    list.add(res.getString("word"))
                }
            }
        }

        return list
    }

    private fun getWarnActionsForGuild(guildId: Long, con: Connection): List<WarnAction> {
        val list = arrayListOf<WarnAction>()

        con.prepareStatement("SELECT * FROM warn_actions WHERE guild_id = ?").use { smt ->
            smt.setString(1, guildId.toString())

            smt.executeQuery().use { res ->
                while (res.next()) {
                    list.add(
                        WarnAction(
                            WarnAction.Type.valueOf(res.getString("type")),
                            res.getInt("threshold"),
                            res.getInt("duration")
                        )
                    )
                }
            }
        }

        return list
    }
}
