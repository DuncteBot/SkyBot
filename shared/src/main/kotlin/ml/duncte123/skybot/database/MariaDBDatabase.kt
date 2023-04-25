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
import com.dunctebot.models.utils.Utils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.sentry.Sentry
import ml.duncte123.skybot.extensions.toGuildSettingMySQL
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.CommandResult
import ml.duncte123.skybot.objects.command.CustomCommand
import java.sql.Connection
import java.sql.SQLException
import java.sql.Types
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

    override fun getCustomCommands() = runOnThread {
        val customCommands = arrayListOf<CustomCommand>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM customCommands").use { res ->
                    while (res.next()) {
                        customCommands.add(
                            CustomCommand(
                                res.getString("invoke"),
                                res.getString("message"),
                                res.getString("guildId").toLong(),
                                res.getBoolean("autoresponse")
                            )
                        )
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
            con.prepareStatement(
                "SELECT COUNT(guildId) as cmd_count FROM customCommands WHERE guildId = ?"
            ).use { smt ->
                smt.setString(1, guildId.toString())

                smt.executeQuery().use { res ->
                    if (res.next() && res.getInt("cmd_count") >= MAX_CUSTOM_COMMANDS) {
                        return@runOnThread CommandResult.LIMIT_REACHED
                    }
                }
            }

            con.prepareStatement(
                "SELECT COUNT(invoke) AS cmd_count FROM customCommands WHERE invoke = ? AND guildId = ?"
            ).use { smt ->
                smt.setString(1, invoke)
                smt.setString(2, guildId.toString())

                smt.executeQuery().use { res ->
                    // Would be funny to see more than one command with the same invoke here.
                    if (res.next() && res.getInt("cmd_count") >= 1) {
                        return@runOnThread CommandResult.COMMAND_EXISTS
                    }
                }
            }

            con.prepareStatement(
                "INSERT INTO customCommands (guildId, invoke, message, autoresponse) VALUES (?,?,?,?)"
            ).use { smt ->
                smt.setString(1, guildId.toString())
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
                "UPDATE customCommands SET message = ?, autoresponse = ? WHERE guildId = ? AND invoke = ?"
            ).use { smt ->
                smt.setString(1, message)
                smt.setBoolean(2, autoresponse)
                smt.setString(3, guildId.toString())
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
            con.prepareStatement("DELETE FROM customCommands WHERE guildId = ? AND invoke = ?").use { smt ->
                smt.setString(1, guildId.toString())
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
                smt.executeQuery("SELECT * FROM guildSettings").use { res ->
                    while (res.next()) {
                        val guildId = res.getLong("guildId")
                        settings.add(
                            res.toGuildSettingMySQL()
                                // be smart and re-use the connection we already have
                                .setEmbedColor(getEmbedColorForGuild(guildId, con))
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
            con.prepareStatement("SELECT * FROM guildSettings WHERE guildId = ?").use { smt ->
                smt.setString(1, guildId.toString())

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        return@runOnThread res.toGuildSettingMySQL()
                            // be smart and re-use the connection we already have
                            .setEmbedColor(getEmbedColorForGuild(guildId, con))
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
            // language=MariaDB
            "DELETE FROM guildSettings WHERE guildId IN",
            // language=MariaDB
            "DELETE FROM vc_auto_roles WHERE guild_id IN",
            // language=MariaDB
            "DELETE FROM guild_blacklists WHERE guild_id IN",
            // language=MariaDB
            "DELETE FROM warn_actions WHERE guild_id IN",
            // language=MariaDB
            "DELETE FROM customCommands WHERE guildId IN"
        )

        val questions = guildIds.joinToString(", ") { "?" }

        this.connection.use { con ->
            queries.forEach { q ->
                // language=MariaDB
                con.prepareStatement("$q ($questions)").use { smt ->
                    guildIds.forEachIndexed { index, id ->
                        smt.setString(index + 1, id.toString())
                    }

                    smt.execute()
                }
            }
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSetting) = runOnThread {
        this.connection.use { con ->
            updateEmbedColor(guildSettings.guildId, guildSettings.embedColor, con)

            // TODO: remove server_description, discord has this feature now
            con.prepareStatement(
                """UPDATE guildSettings SET
                    |prefix = ?,
                    |autoRole = ?,
                    |leave_timeout = ?,
                    |announceNextTrack = ?,
                    |allowAllToStop = ?,
                    |serverDesc = ?,
                    |
                    |welcomeLeaveChannel = ?,
                    |enableJoinMessage = ?,
                    |enableLeaveMessage = ?,
                    |enableJoinMessage = ?,
                    |customLeaveMessage = ?,
                    |
                    |logChannelId = ?,
                    |muteRoleId = ?,
                    |enableSwearFilter = ?,
                    |filterType = ?,
                    |aiSensitivity = ?,
                    |
                    |autoDeHoist = ?,
                    |filterInvites = ?,
                    |spamFilterState = ?,
                    |kickInsteadState = ?,
                    |ratelimits = ?,
                    |spam_threshold = ?,
                    |young_account_ban_enabled = ?,
                    |young_account_threshold = ?,
                    |
                    |banLogging = ?,
                    |unbanLogging = ?,
                    |muteLogging = ?,
                    |warnLogging = ?,
                    |memberLogging = ?,
                    |invite_logging = ?,
                    |message_logging = ?
                    |
                    |WHERE guildId = ?
                """.trimMargin()
            ).use { smt ->
                /// <editor-fold defaultstate="collapsed" desc="guild settings">
                smt.setString(1, guildSettings.customPrefix)
                smt.setLong(2, guildSettings.autoroleRole)
                smt.setInt(3, guildSettings.leaveTimeout)
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
                smt.setString(21, Utils.convertJ2S(guildSettings.ratelimits))
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
                smt.setString(32, guildSettings.guildId.toString())
                /// </editor-fold>

                return@runOnThread smt.execute()
            }
        }
    }

    override fun registerNewGuild(guildSettings: GuildSetting) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                """INSERT IGNORE INTO guildSettings(guildId, prefix, customWelcomeMessage, customLeaveMessage) 
                |VALUES (?, ?, ?, ?);
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
                "INSERT IGNORE INTO guild_blacklists(guild_id, word) VALUES $vals"
            ).use { smt ->
                var paramIndex = 0
                words.forEach { word ->
                    smt.setString(++paramIndex, guildId.toString())
                    smt.setString(++paramIndex, word)
                }

                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM guild_blacklists WHERE guild_id = ? AND word = ?").use { smt ->
                smt.setString(1, guildId.toString())
                smt.setString(2, word)

                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun clearBlacklist(guildId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("DELETE FROM guild_blacklists WHERE guild_id = ?").use { smt ->
                smt.setLong(1, guildId)
                smt.execute()
            }
        }

        return@runOnThread
    }

    @Deprecated("Stored in guild settings")
    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) = runOnThread {
        TODO("Not yet implemented")
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
                        val idRes = res.getString("guild_id").toLong()
                        val guildId = if (idRes == 0L) null else idRes
                        val patron = Patron(
                            Patron.Type.valueOf(res.getString("type").uppercase()),
                            res.getString("user_id").toLong(),
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
        }

        return@runOnThread AllPatronsData(patrons, tagPatrons, oneGuildPatrons, guildPatrons)
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
                    |VALUES (?, ?, ?) ON DUPLICATE KEY
                    |UPDATE type = ?, guild_id = ?
                """.trimMargin()
            ).use { smt ->
                smt.setString(1, patron.userId.toString())
                smt.setString(2, patron.type.name)
                smt.setString(4, patron.type.name)

                if (patron.guildId == null) {
                    smt.setNull(3, Types.CHAR)
                    smt.setNull(5, Types.CHAR)
                } else {
                    smt.setString(3, patron.guildId.toString())
                    smt.setString(5, patron.guildId.toString())
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
                    } else {
                        return@runOnThread null
                    }
                }
            }
        }
    }

    override fun createBan(
        modId: Long,
        userId: Long,
        unbanDate: String,
        guildId: Long
    ): CompletableFuture<Unit> = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO bans (userId, modUserId, guildId, unban_date, ban_date, Username, discriminator) VALUES (?, ?, ?, ?, now(), 'UNUSED', '0000')"
            ).use { smt ->
                smt.setString(1, userId.toString())
                smt.setString(2, modId.toString())
                smt.setString(3, guildId.toString())
                // TODO: this should be a date datatype
                smt.setString(4, unbanDate)
                smt.execute()
            }
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String): CompletableFuture<Unit> = runOnThread {
        this.connection.use { con ->
            con.prepareStatement(
                "INSERT INTO warnings(user_id, mod_id, guild_id, warn_date, reason, expire_date) VALUES (?, ?, ?, now(), ?, now() + interval 6 DAY)"
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

            con.prepareStatement("SELECT * FROM mutes WHERE guild_id = ? AND user_id = ?").use { smt ->
                smt.setString(1, guildId.toString())
                smt.setString(2, userId.toString())

                smt.executeQuery().use { res ->
                    if (res.next()) {
                        oldMute = Mute(
                            res.getInt("id"),
                            res.getString("mod_id").toLong(),
                            res.getString("user_id").toLong(),
                            "",
                            res.getString("guild_id").toLong()
                        )
                    }
                }
            }

            if (oldMute != null) {
                con.prepareStatement("DELETE FROM mutes WHERE id = ?").use { smt ->
                    smt.setInt(1, oldMute!!.id)
                    smt.execute()
                }
            }

            con.prepareStatement(
                "INSERT INTO mutes(user_id, mod_id, guild_id, unmute_date, user_tag) VALUES (?, ?, ?, ?, 'UNKNOWN#0000')"
            ).use { smt ->
                smt.setString(1, userId.toString())
                smt.setString(2, modId.toString())
                smt.setString(3, guildId.toString())
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
                "SELECT * FROM warnings WHERE user_id = ? AND guild_id = ? AND (now() > (warn_date - INTERVAL 6 DAY))"
            ).use { smt ->
                smt.setString(1, userId.toString())
                smt.setString(2, guildId.toString())

                smt.executeQuery().use { res ->
                    while (res.next()) {
                        warnings.add(
                            Warning(
                                res.getInt("id"),
                                res.getString("warn_date"),
                                res.getString("mod_id").toLong(),
                                res.getString("reason"),
                                res.getString("guild_id").toLong()
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
                "SELECT COUNT(id) as amount FROM warnings WHERE user_id = ? AND guild_id = ? AND (now() > (warn_date - INTERVAL 6 DAY))"
            ).use { smt ->
                smt.setString(1, userId.toString())
                smt.setString(2, guildId.toString())

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
                                res.getString("mod_id").toLong(),
                                res.getString("reason"),
                                res.getString("guild_id").toLong()
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

    override fun getExpiredBansAndMutes() = runOnThread {
        val bans = mutableListOf<Ban>()
        val mutes = mutableListOf<Mute>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM bans WHERE unban_date <= now()").use { res ->
                    while (res.next()) {
                        bans.add(
                            Ban(
                                res.getInt("id"),
                                res.getString("modUserId"),
                                res.getString("userId").toLong(),
                                "Deleted User",
                                "0000",
                                res.getString("guildId")
                            )
                        )
                    }
                }
            }

            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM mutes WHERE unmute_date <= now()").use { res ->
                    while (res.next()) {
                        mutes.add(
                            Mute(
                                res.getInt("id"),
                                res.getString("mod_id").toLong(),
                                res.getString("user_id").toLong(),
                                "Deleted User#0000",
                                res.getString("guild_id").toLong()
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
            con.prepareStatement("DELETE FROM bans WHERE id IN ($values)").use { smt ->
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
            con.prepareStatement("DELETE FROM mutes WHERE id IN ($values)").use { smt ->
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
                "INSERT IGNORE INTO ban_bypasses(guild_id, user_id) VALUES (?, ?)"
            ).use { smt ->
                smt.setString(1, guildId.toString())
                smt.setString(2, userId.toString())
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun getBanBypass(guildId: Long, userId: Long) = runOnThread {
        this.connection.use { con ->
            con.prepareStatement("SELECT * FROM ban_bypasses WHERE guild_id = ? AND user_id = ?").use { smt ->
                smt.setString(1, guildId.toString())
                smt.setString(2, userId.toString())

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
                smt.setString(1, banBypass.guildId.toString())
                smt.setString(2, banBypass.userId.toString())
                smt.execute()
            }
        }

        return@runOnThread
    }

    override fun getVcAutoRoles() = runOnThread {
        val roles = mutableListOf<VcAutoRole>()

        this.connection.use { con ->
            con.createStatement().use { smt ->
                smt.executeQuery("SELECT * FROM vc_auto_roles").use { res ->
                    while (res.next()) {
                        roles.add(
                            VcAutoRole(
                                res.getString("guild_id").toLong(),
                                res.getString("voice_channel_id").toLong(),
                                res.getString("role_id").toLong()
                            )
                        )
                    }
                }
            }
        }

        return@runOnThread roles.toList()
    }

    // TODO: constraint in database
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
                                res.getString("owner_id").toLong()
                            )
                        )
                    }
                }
            }
        }

        return@runOnThread tags.toList()
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

    override fun close() {
        this.ds.close()
    }

    private fun getEmbedColorForGuild(guildId: Long, con: Connection): Int {
        con.prepareStatement("SELECT embed_color FROM embedSettings WHERE guild_id =?").use { smt ->
            smt.setString(1, guildId.toString())

            smt.executeQuery().use { res ->
                if (res.next()) {
                    return res.getInt("embed_color")
                }
            }
        }


        return -1
    }

    private fun updateEmbedColor(guildId: Long, color: Int, con: Connection) {
        con.prepareStatement(
            "INSERT INTO embedSettings (guild_id, embed_color) VALUES(?, ?) ON DUPLICATE KEY UPDATE embed_color = ?"
        ).use { smt ->
            smt.setString(1, guildId.toString())
            smt.setInt(2, color)
            smt.setInt(3, color)

            smt.executeUpdate()
        }
    }

    private fun getBlackListsForGuild(guildId: Long, con: Connection): List<String> {
        val list = arrayListOf<String>()

        con.prepareStatement("SELECT word FROM guild_blacklists WHERE guild_id = ?").use { smt ->
            smt.setString(1, guildId.toString())

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
