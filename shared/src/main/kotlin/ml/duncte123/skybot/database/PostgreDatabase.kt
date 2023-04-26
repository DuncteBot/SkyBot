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
import io.sentry.Sentry
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import ml.duncte123.skybot.extensions.toGuildSetting
import ml.duncte123.skybot.extensions.toReminder
import ml.duncte123.skybot.extensions.toSQL
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CommandResult
import ml.duncte123.skybot.objects.command.CustomCommand
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

class PostgreDatabase(jdbcURI: String, ohShitFn: (Int, Int) -> Unit = { _, _ -> }) : AbstractDatabase(2, ohShitFn) {
    private val ds: HikariDataSource
    private val connection: Connection
        get() {
            return this.ds.connection
        }

    init {
        val config = HikariConfig()

        // IT IS postgresql:// NOT psql://
        config.jdbcUrl = jdbcURI // &ssl.mode=require

        this.ds = HikariDataSource(config)
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

    override fun getCustomCommands() = runOnThread {
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

        return@runOnThread customCommands.toList()
    }

    override fun createCustomCommand(
        guildId: Long,
        invoke: String,
        message: String
    ) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT COUNT(guild_id) AS cmd_count FROM custom_commands WHERE guild_id = ?")
                .use { smt ->
                    smt.setLong(1, guildId)

                    smt.executeQuery().use { res ->
                        if (res.next() && res.getInt("cmd_count") >= MAX_CUSTOM_COMMANDS) {
                            return@runOnThread CommandResult.LIMIT_REACHED
                        }
                    }
                }

            con.prepareStatement(
                "SELECT COUNT(invoke) AS cmd_count FROM root.public.custom_commands WHERE invoke = ? AND guild_id = ?"
            ).use { smt ->
                smt.setString(1, invoke)
                smt.setLong(2, guildId)

                smt.executeQuery().use { res ->
                    // Would be funny to see more than one command with the same invoke here.
                    if (res.next() && res.getInt("cmd_count") >= 1) {
                        return@runOnThread CommandResult.COMMAND_EXISTS
                    }
                }
            }

            con.prepareStatement("INSERT INTO custom_commands(guild_id, invoke, message, auto_response) VALUES (?, ?, ?, ?)")
                .use { smt ->
                    smt.setLong(1, guildId)
                    smt.setString(2, invoke)
                    smt.setString(3, message)
                    smt.setBoolean(4, false)

                    try {
                        smt.execute()
                        return@runOnThread CommandResult.SUCCESS
                    } catch (e: SQLException) {
                        Sentry.captureException(e)
                        return@runOnThread CommandResult.UNKNOWN
                    }
                }
        }
    }

    override fun updateCustomCommand(
        guildId: Long,
        invoke: String,
        message: String,
        autoresponse: Boolean
    ) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "UPDATE custom_commands SET message = ?, auto_response = ? WHERE guild_id = ? AND invoke = ?"
            ).use { smt ->
                smt.setString(1, message)
                smt.setBoolean(2, autoresponse)
                smt.setLong(3, guildId)
                smt.setString(4, invoke)

                try {
                    smt.executeUpdate()
                    return@runOnThread CommandResult.SUCCESS
                } catch (e: SQLException) {
                    Sentry.captureException(e)
                    return@runOnThread CommandResult.UNKNOWN
                }
            }
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM custom_commands WHERE guild_id = ? AND invoke = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.setString(2, invoke)

                try {
                    smt.execute()
                    return@runOnThread true
                } catch (e: SQLException) {
                    Sentry.captureException(e)
                    return@runOnThread false
                }
            }
        }
    }

    override fun getGuildSettings() = runOnThread {
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

        return@runOnThread settings.toList()
    }

    override fun loadGuildSetting(guildId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM guild_settings WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread res.toGuildSetting()
                            // be smart and re-use the connection we already have
                            .setBlacklistedWords(getBlackListsForGuild(guildId, con))
                            .setWarnActions(getWarnActionsForGuild(guildId, con))
                    }
                }
            }
        }

        return@runOnThread null
    }

    override fun purgeGuildSettings(guildIds: List<Long>) = runOnThread {
        val queries = arrayOf(
            "DELETE FROM guild_settings WHERE guild_id IN",
            "DELETE FROM vc_autoroles WHERE guild_id IN",
            "DELETE FROM blacklisted_words WHERE guild_id IN",
            "DELETE FROM warn_actions WHERE guild_id IN",
            "DELETE FROM custom_commands WHERE guild_id IN"
        )
        val questions = guildIds.joinToString(", ") { "?" }

        this.connection.use { con ->
            queries.forEach { q ->
                con.prepareStatement("$q ($questions)").use { smt ->
                    guildIds.forEachIndexed { index, id ->
                        smt.setLong(index + 1, id)
                    }

                    smt.execute()
                }
            }
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSetting) = runOnThread {
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
                smt.setBoolean(5, guildSettings.isAnnounceTracks)
                smt.setBoolean(6, guildSettings.isAllowAllToStop)
                // TODO: remove, discord has this feature
                smt.setString(7, guildSettings.serverDesc)

                smt.setLong(8, guildSettings.welcomeLeaveChannel)
                smt.setBoolean(9, guildSettings.isEnableJoinMessage)
                smt.setBoolean(10, guildSettings.isEnableLeaveMessage)
                smt.setString(11, guildSettings.customJoinMessage)
                smt.setString(12, guildSettings.customLeaveMessage)

                smt.setLong(13, guildSettings.logChannel)
                smt.setLong(14, guildSettings.muteRoleId)
                smt.setBoolean(15, guildSettings.isEnableSwearFilter)
                smt.setString(16, guildSettings.filterType.type)
                smt.setFloat(17, guildSettings.aiSensitivity)

                smt.setBoolean(18, guildSettings.isAutoDeHoist)
                smt.setBoolean(19, guildSettings.isFilterInvites)
                smt.setBoolean(20, guildSettings.isEnableSpamFilter)
                smt.setBoolean(21, guildSettings.kickState)
                smt.setString(22, convertJ2S(guildSettings.ratelimits))
                smt.setInt(23, guildSettings.spamThreshold)
                smt.setBoolean(24, guildSettings.isYoungAccountBanEnabled)
                smt.setInt(25, guildSettings.youngAccountThreshold)

                // Logging :)
                smt.setBoolean(26, guildSettings.isBanLogging)
                smt.setBoolean(27, guildSettings.isUnbanLogging)
                smt.setBoolean(28, guildSettings.isMuteLogging)
                smt.setBoolean(29, guildSettings.isWarnLogging)
                smt.setBoolean(30, guildSettings.isMemberLogging)
                smt.setBoolean(31, guildSettings.isInviteLogging)
                smt.setBoolean(32, guildSettings.isMessageLogging)

                // What guild?
                smt.setLong(33, guildSettings.guildId)

                return@runOnThread smt.execute()
            }
        }
    }

    override fun registerNewGuild(guildSettings: GuildSetting) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                """INSERT INTO guild_settings(guild_id, prefix, join_message, leave_message) 
                |VALUES (?, ?, ?, ?) 
                |ON CONFLICT (guild_id) DO NOTHING;
                """.trimMargin()
            ).use { smt ->
                smt.setLong(1, guildSettings.guildId)
                smt.setString(2, guildSettings.customPrefix)
                smt.setString(3, guildSettings.customJoinMessage)
                smt.setString(4, guildSettings.customLeaveMessage)

                return@runOnThread smt.execute()
            }
        }
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) = runOnThread {
        val vals = words.joinToString(", ") { "(?, ?)" }

        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO blacklisted_words(guild_id, word) VALUES $vals ON CONFLICT (guild_id, word) DO NOTHING /* LOL */"
            ).use { smt ->
                var paramIndex = 0
                words.forEach { word ->
                    smt.setLong(++paramIndex, guildId)
                    smt.setString(++paramIndex, word)
                }

                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM blacklisted_words WHERE guild_id = ? AND word = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.setString(2, word)

                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun clearBlacklist(guildId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM blacklisted_words WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun loadAllPatrons() = runOnThread {
        val patrons = mutableListOf<Patron>()
        val tagPatrons = mutableListOf<Patron>()
        val oneGuildPatrons = mutableListOf<Patron>()
        val guildPatrons = mutableListOf<Patron>()

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

            return@runOnThread AllPatronsData(patrons, tagPatrons, oneGuildPatrons, guildPatrons)
        }
    }

    override fun removePatron(userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM patrons WHERE user_id = ?").use { smt ->
                smt.setLong(1, userId)

                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun createOrUpdatePatron(patron: Patron) = runOnThread {
        this.createOrUpdatePatronSync(patron)
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long) = runOnThread {
        this.createOrUpdatePatronSync(Patron(Patron.Type.ONE_GUILD, userId, guildId))

        return@runOnThread userId to guildId
    }

    private fun createOrUpdatePatronSync(patron: Patron) {
        this.connection.use { con ->
            con.prepareStatement(
                """INSERT INTO patrons(user_id, type, guild_id)
                    |VALUES (?, ?, ?) ON CONFLICT (user_id)
                    |DO UPDATE SET type = ?, guild_id = ?
                """.trimMargin()
            ).use { smt ->
                smt.setLong(1, patron.userId)
                smt.setString(2, patron.type.name)
                smt.setString(4, patron.type.name)

                if (patron.guildId == null) {
                    smt.setNull(3, Types.BIGINT)
                    smt.setNull(5, Types.BIGINT)
                } else {
                    smt.setLong(3, patron.guildId)
                    smt.setLong(5, patron.guildId)
                }

                smt.execute()
            }
        }
    }

    override fun getOneGuildPatron(userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT guild_id FROM patrons WHERE user_id = ? AND type = ?").use { smt ->
                smt.setLong(1, userId)
                smt.setString(2, Patron.Type.ONE_GUILD.name)

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread res.getLong("guild_id")
                    }
                }
            }
        }

        return@runOnThread null
    }

    override fun createBan(
        modId: Long,
        userId: Long,
        unbanDate: String,
        guildId: Long
    ): CompletableFuture<Unit> = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO temp_bans (user_id, mod_id, guild_id, unban_date) VALUES (?, ?, ?, ?)"
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, modId)
                smt.setLong(3, guildId)
                // TODO: this should be a date datatype
                smt.setString(4, unbanDate)
                smt.execute()
            }
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String): CompletableFuture<Unit> = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO warnings(user_id, mod_id, guild_id, warn_date, reason) VALUES (?, ?, ?, now(), ?)"
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, modId)
                smt.setLong(3, guildId)
                smt.setString(4, reason)
                smt.execute()
            }
        }
    }

    override fun createMute(
        modId: Long,
        userId: Long,
        userTag: String,
        unmuteDate: String,
        guildId: Long
    ) = runOnThread {
        this.connection.use { con ->
            var oldMute: Mute? = null

            con.prepareStatement("SELECT * FROM temp_mutes WHERE guild_id = ? AND user_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.setLong(2, userId)

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        oldMute = Mute(
                            res.getInt("id"),
                            res.getLong("mod_id"),
                            res.getLong("user_id"),
                            "",
                            res.getLong("guild_id")
                        )
                    }
                }
            }

            if (oldMute != null) {
                con.prepareStatement("DELETE FROM temp_mutes WHERE id = ?").use { smt ->
                    smt.setInt(1, oldMute!!.id)
                    smt.execute()
                }
            }

            con.prepareStatement(
                "INSERT INTO temp_mutes(user_id, mod_id, guild_id, unmute_date) VALUES (?, ?, ?, ?)"
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, modId)
                smt.setLong(3, guildId)
                smt.setString(4, unmuteDate)
                smt.execute()
            }

            return@runOnThread oldMute
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long) = runOnThread {
        val warnings = mutableListOf<Warning>()

        this.connection.use { con ->
            con.prepareStatement(
                "SELECT * FROM warnings WHERE user_id = ? AND guild_id = ? AND (now() > (warn_date - '6 day'::interval))"
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, guildId)
                smt.executeQuery().use { res ->
                    while (res.next()) {
                        warnings.add(
                            Warning(
                                res.getInt("id"),
                                res.getString("warn_date"),
                                res.getLong("mod_id"),
                                res.getString("reason"),
                                res.getLong("guild_id")
                            )
                        )
                    }
                }
            }
        }

        return@runOnThread warnings.toList()
    }

    override fun getWarningCountForUser(userId: Long, guildId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "SELECT COUNT(id) as amount FROM warnings WHERE user_id = ? AND guild_id = ? AND (now() > (warn_date - '6 day'::interval))"
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, guildId)

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread res.getInt("amount")
                    }
                }
            }
        }

        return@runOnThread 0
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long) = runOnThread {
        var oldWarning: Warning? = null

        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM warnings WHERE user_id = ? AND guild_id = ? ORDER BY id DESC LIMIT 1")
                .use { smt ->
                    smt.setLong(1, userId)
                    smt.setLong(2, guildId)
                    smt.executeQuery().use { res ->
                        if (res.next()) {
                            oldWarning = Warning(
                                res.getInt("id"),
                                res.getString("warn_date"),
                                res.getLong("mod_id"),
                                res.getString("reason"),
                                res.getLong("guild_id")
                            )
                        }
                    }
                }

            if (oldWarning != null) {
                con.prepareStatement("DELETE FROM warnings WHERE id = ?").use { smt ->
                    smt.setInt(1, oldWarning!!.id)
                    smt.executeUpdate()
                }
            }
        }

        return@runOnThread oldWarning
    }

    override fun purgeExpiredWarnings() = runOnThread {
        val warningIds = mutableListOf<Int>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM warnings WHERE now() > (warn_date - '6 day'::interval)").use { res ->
                    while (res.next()) {
                        warningIds.add(res.getInt("id"))
                    }
                }
            }

            val values = warningIds.joinToString(", ") { "?" }

            con.prepareStatement("DELETE FROM warnings WHERE id in ($values)").use { smt ->
                warningIds.forEachIndexed { index, id ->
                    smt.setInt(index + 1, id)
                }
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun getExpiredBansAndMutes() = runOnThread {
        val bans = mutableListOf<Ban>()
        val mutes = mutableListOf<Mute>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM temp_bans WHERE unban_date <= now()").use { res ->
                    while (res.next()) {
                        bans.add(
                            Ban(
                                res.getInt("id"),
                                res.getString("mod_id"),
                                res.getLong("user_id"),
                                "Deleted User",
                                "0000",
                                res.getString("guild_id")
                            )
                        )
                    }
                }
            }

            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM temp_mutes WHERE unmute_date <= now()").use { res ->
                    while (res.next()) {
                        mutes.add(
                            Mute(
                                res.getInt("id"),
                                res.getLong("mod_id"),
                                res.getLong("user_id"),
                                "Deleted User#0000",
                                res.getLong("guild_id")
                            )
                        )
                    }
                }
            }
        }

        return@runOnThread bans.toList() to mutes.toList()
    }

    override fun purgeBans(ids: List<Int>) = runOnThread {
        this.connection.use { con ->
            val values = ids.joinToString(", ") { "?" }
            con.prepareStatement("DELETE FROM temp_bans WHERE id IN ($values)").use { smt ->
                ids.forEachIndexed { index, id ->
                    smt.setInt(index + 1, id)
                }
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun purgeMutes(ids: List<Int>) = runOnThread {
        this.connection.use { con ->
            val values = ids.joinToString(", ") { "?" }
            con.prepareStatement("DELETE FROM temp_mutes WHERE id IN ($values)").use { smt ->
                ids.forEachIndexed { index, id ->
                    smt.setInt(index + 1, id)
                }
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun createBanBypass(guildId: Long, userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO ban_bypasses(guild_id, user_id) VALUES (?, ?) ON CONFLICT (guild_id, user_id) DO NOTHING"
            ).use { smt ->
                smt.setLong(1, guildId)
                smt.setLong(2, userId)
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun getBanBypass(guildId: Long, userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM ban_bypasses WHERE guild_id = ? AND user_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.setLong(2, userId)
                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread BanBypas(res.getLong("guild_id"), res.getLong("user_id"))
                    }
                }
            }
        }

        return@runOnThread null
    }

    override fun deleteBanBypass(banBypass: BanBypas) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM ban_bypasses WHERE guild_id = ? AND user_id = ?").use { smt ->
                smt.setLong(1, banBypass.guildId)
                smt.setLong(2, banBypass.userId)
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun getVcAutoRoles() = runOnThread {
        val roles = mutableListOf<VcAutoRole>()

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

        return@runOnThread roles.toList()
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) =
        setVcAutoRoleBatch(guildId, listOf(voiceChannelId), roleId)

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) = runOnThread {
        val values = voiceChannelIds.joinToString(", ") { "(?, ?, ?)" }

        this.connection.use { con ->
            con.prepareStatement(
                """INSERT INTO vc_autoroles (guild_id, voice_channel_id, role_id)
                    |VALUES $values
                    |ON CONFLICT (guild_id, voice_channel_id, role_id) DO NOTHING
                """.trimMargin()
            ).use { smt ->
                var paramIndex = 0
                voiceChannelIds.forEach { voiceChannelId ->
                    smt.setLong(++paramIndex, guildId)
                    smt.setLong(++paramIndex, voiceChannelId)
                    smt.setLong(++paramIndex, roleId)
                }
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun removeVcAutoRole(voiceChannelId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM vc_autoroles WHERE voice_channel_id = ?").use { smt ->
                smt.setLong(1, voiceChannelId)
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM vc_autoroles WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.execute()
            }
        }
        return@runOnThread
    }

    override fun loadTags() = runOnThread {
        val tags = mutableListOf<Tag>()

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

        return@runOnThread tags.toList()
    }

    override fun createTag(tag: Tag) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("INSERT INTO tags(owner_id, name, content) VALUES(?, ?, ?)").use { smt ->
                smt.setLong(1, tag.ownerId)
                smt.setString(2, tag.name)
                smt.setString(3, tag.content)

                try {
                    smt.execute()
                    return@runOnThread true to ""
                } catch (e: SQLException) {
                    return@runOnThread false to (e.message ?: "Unknown failure")
                }
            }
        }
    }

    override fun deleteTag(tag: Tag) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM tags WHERE id = ?").use { smt ->
                smt.setInt(1, tag.id)
                smt.execute()
            }
        }

        // TODO: figure out what I meant by the statement below
        // TODO: failures to not work
        return@runOnThread true to ""
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
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO reminders(user_id, guild_id, channel_id, message_id, in_channel, reminder, remind_on) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf("id") // cols to return
            ).use { smt ->
                smt.setLong(1, userId)
                smt.setLong(2, guildId)
                smt.setLong(3, channelId)
                smt.setLong(4, messageId)
                smt.setBoolean(5, inChannel)
                smt.setString(6, reminder)
                smt.setDate(7, expireDate.toSQL())

                try {
                    smt.execute()

                    smt.generatedKeys.use { res ->
                        if (res.next()) {
                            return@runOnThread true to res.getInt("id")
                        }
                    }
                } catch (ex: SQLException) {
                    Sentry.captureException(ex)
                }
            }
        }

        return@runOnThread false to -1
    }

    override fun removeReminder(reminderId: Int, userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM reminders WHERE id = ? AND user_id = ?").use { smt ->
                smt.setInt(1, reminderId)
                smt.setLong(2, userId)
                smt.execute()
            }
        }

        return@runOnThread true
    }

    override fun showReminder(reminderId: Int, userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM reminders WHERE id = ? AND user_id = ?").use { smt ->
                smt.setInt(1, reminderId)
                smt.setLong(2, userId)
                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread res.toReminder()
                    }
                }
            }
        }

        return@runOnThread null
    }

    override fun listReminders(userId: Long) = runOnThread {
        val reminders = arrayListOf<Reminder>()
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM reminders WHERE user_id = ?").use { smt ->
                smt.setLong(1, userId)
                smt.executeQuery().use { res ->
                    while (res.next()) {
                        reminders.add(res.toReminder())
                    }
                }
            }
        }

        return@runOnThread reminders.toList()
    }

    override fun getExpiredReminders() = runOnThread {
        val reminders = mutableListOf<Reminder>()

        this.connection.use { con ->
            con.createStatement().executeQuery("SELECT * FROM reminders WHERE now() >= remind_on").use { res ->
                while (res.next()) {
                    reminders.add(res.toReminder())
                }
            }
        }

        return@runOnThread reminders.toList()
    }

    override fun purgeReminders(ids: List<Int>) = runOnThread {
        val question = ids.joinToString(", ") { "?" }

        this.connection.use { con ->
            con.prepareStatement("DELETE FROM reminders WHERE id IN ($question)").use { smt ->
                ids.forEachIndexed { index, id ->
                    smt.setInt(index + 1, id)
                }
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM warn_actions WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.execute()
            }

            val spots = actions.joinToString(", ") { "(?, ?, ?, ?)" }
            con.prepareStatement("INSERT INTO warn_actions(guild_id, type, threshold, duration) VALUES $spots")
                .use { smt ->
                    var paramIndex = 0

                    actions.forEach {
                        smt.setLong(++paramIndex, guildId)
                        smt.setString(++paramIndex, it.type.name)
                        smt.setInt(++paramIndex, it.threshold)
                        smt.setInt(++paramIndex, it.duration)
                    }

                    smt.execute()
                }
        }

        return@runOnThread
    }

    override fun close() {
        this.ds.close()
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
            smt.setLong(1, guildId)

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
