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
import gnu.trove.map.hash.TLongIntHashMap
import gnu.trove.map.hash.TLongLongHashMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.database.SQLiteDatabaseConnectionManager
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.*
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.objects.guild.WarnAction
import ml.duncte123.skybot.utils.AirUtils.fromDatabaseFormat
import ml.duncte123.skybot.utils.GuildSettingsUtils.*
import java.io.File
import java.sql.ResultSet
import java.sql.SQLException
import java.time.Instant
import java.time.temporal.TemporalAccessor
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class SqliteDatabaseAdapter : DatabaseAdapter(1) {
    private val connManager = SQLiteDatabaseConnectionManager(File("database.db"))

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        runOnThread {
            val customCommands = arrayListOf<CustomCommand>()
            // language=SQLite
            val res = connManager.connection.createStatement().executeQuery("SELECT * FROM customCommands")

            while (res.next()) {
                customCommands.add(CustomCommandImpl(
                    res.getString("invoke"),
                    res.getString("message"),
                    res.getLong("guildId"),
                    res.getBoolean("autoresponse")
                ))
            }
            callback.invoke(customCommands)
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            val res = changeCommand(guildId, invoke, message, false)

            callback.invoke(res)
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        runOnThread {
            val res = changeCommand(guildId, invoke, message, true)

            callback.invoke(res)
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?) {
        runOnThread {
            // language=SQLite
            connManager.connection.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?").use {
                it.setString(1, invoke)
                it.setString(2, guildId.toString())
                it.execute()
                it.closeOnCompletion()
            }

            callback.invoke(true)
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        runOnThread {

            val settings = arrayListOf<GuildSettings>()
            val smt = connManager.connection.createStatement()

            // language=SQLite
            val res = smt.executeQuery("SELECT * FROM guildSettings")

            while (res.next()) {
                val guildId = toLong(res.getString("guildId"))
                settings.add(res.toGuildSettings(guildId))
            }
            callback.invoke(settings)
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        runOnThread {
            // language=SQLite
            connManager.connection.prepareStatement("INSERT INTO blacklists(guild_id, word) VALUES( ? , ? )").use {
                it.setString(1, guildId.toString())
                it.setString(2, word)

                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun addWordsToBlacklist(guildId: Long, words: List<String>) {
        words.forEach {
            addWordToBlacklist(guildId, it)
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        runOnThread {
            // language=SQLite
            connManager.connection.prepareStatement("DELETE FROM blacklists WHERE guild_id = ? AND word = ?").use {

                it.setString(1, guildId.toString())
                it.setString(2, word)

                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun clearBlacklist(guildId: Long) {
        runOnThread {
            val connection = connManager.connection

            // language=SQLite
            connection.createStatement().execute("DELETE FROM blacklists where guild_id = '$guildId'")
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings?) -> Unit) {
        runOnThread {
            // language=SQLite
            val res = connManager.connection.createStatement().executeQuery("SELECT * FROM guildSettings WHERE guildId = '$guildId'")

            while (res.next()) {
                callback.invoke(res.toGuildSettings(guildId))

                return@runOnThread
            }
            callback.invoke(null)
        }
    }

    override fun deleteGuildSetting(guildId: Long) {
        runOnThread {
            val connection = connManager.connection

            // language=SQLite
            connection.createStatement().execute("DELETE FROM guildSettings where guildId = '$guildId'")
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            // language=SQLite
            connManager.connection.prepareStatement("""UPDATE guildSettings SET 
                enableJoinMessage= ? ,
                enableSwearFilter= ? ,
                customWelcomeMessage= ? ,
                prefix= ? ,
                autoRole= ? ,
                logChannelId= ? ,
                welcomeLeaveChannel= ? ,
                customLeaveMessage = ? ,
                serverDesc = ? ,
                announceNextTrack = ? ,
                autoDeHoist = ? ,
                filterInvites = ? ,
                spamFilterState = ? ,
                muteRoleId = ? ,
                ratelimits = ? ,
                kickInsteadState = ? ,
                leave_timeout = ? ,
                spam_threshold = ? ,
                logBan = ? ,
                logUnban = ? ,
                logKick = ? ,
                logMute = ? ,
                logWarn = ? ,
                profanity_type = ? ,
                aiSensitivity = ?,
                allow_all_to_stop = ?
                WHERE guildId='${guildSettings.guildId}'
                """.trimMargin()
            ).use {
                it.setBoolean(1, guildSettings.isEnableJoinMessage)
                it.setBoolean(2, guildSettings.isEnableSwearFilter)
                it.setString(3, fixUnicodeAndLines(guildSettings.customJoinMessage))
                it.setString(4, replaceUnicode(guildSettings.customPrefix))
                it.setString(5, guildSettings.autoroleRole.toString())
                it.setString(6, guildSettings.logChannel.toString())
                it.setString(7, guildSettings.welcomeLeaveChannel.toString())
                it.setString(8, fixUnicodeAndLines(guildSettings.customLeaveMessage))
                it.setString(9, fixUnicodeAndLines(guildSettings.serverDesc))
                it.setBoolean(10, guildSettings.isAnnounceTracks)
                it.setBoolean(11, guildSettings.isAutoDeHoist)
                it.setBoolean(12, guildSettings.isFilterInvites)
                it.setBoolean(13, guildSettings.isEnableSpamFilter)
                it.setString(14, guildSettings.muteRoleId.toString())
                it.setString(15, convertJ2S(guildSettings.ratelimits))
                it.setBoolean(16, guildSettings.kickState)
                it.setInt(17, guildSettings.leaveTimeout)
                it.setInt(18, guildSettings.spamThreshold)
                it.setBoolean(19, guildSettings.isBanLogging)
                it.setBoolean(20, guildSettings.isUnbanLogging)
                it.setBoolean(21, guildSettings.isKickLogging)
                it.setBoolean(22, guildSettings.isMuteLogging)
                it.setBoolean(23, guildSettings.isWarnLogging)
                it.setString(24, guildSettings.filterType.type)
                it.setFloat(25, guildSettings.aiSensitivity)
                it.setBoolean(26, guildSettings.isAllowAllToStop)
                it.executeUpdate()
            }
        }

        callback.invoke(true)
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        runOnThread {
            val guildId = guildSettings.guildId
            val connection = connManager.connection
            val resultSet = connection.createStatement()
                // language=SQLite
                .executeQuery("SELECT id FROM guildSettings WHERE guildId='$guildId'")

            var rows = 0

            while (resultSet.next()) {
                rows++
            }

            if (rows == 0) {
                // language=SQLite
                val smt = connection.prepareStatement("""INSERT INTO guildSettings
                    (guildId, customWelcomeMessage, prefix, customLeaveMessage, ratelimits)
                    VALUES('$guildId' , ? , ? , ? , ?)""".trimMargin())

                smt.setString(1, guildSettings.customJoinMessage)
                smt.setString(2, Settings.PREFIX)
                smt.setString(3, guildSettings.customLeaveMessage.replace("\\P{Print}".toRegex(), ""))
                smt.setString(4, "20|45|60|120|240|2400".replace("\\P{Print}".toRegex(), ""))
                smt.execute()
            }

            callback.invoke(true)
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        runOnThread {
            val map = TLongIntHashMap()
            val smt = connManager.connection.createStatement()

            val res = smt.executeQuery("SELECT * FROM embedSettings")

            while (res.next()) {
                map.put(res.getLong("guild_id"), res.getInt("embed_color"))
            }

            callback.invoke(map)
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        runOnThread {
            connManager.connection.prepareStatement(
                "INSERT INTO embedSettings(guild_id, embed_color) VALUES( ? , ? ) ON CONFLICT(guild_id) DO UPDATE SET embed_color = ?"
            ).use {
                it.setString(1, guildId.toString())
                it.setInt(2, color)
                it.setInt(3, color)

                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun loadAllPatrons(callback: (AllPatronsData) -> Unit) {
        runOnThread {
            val patrons = arrayListOf<Patron>()
            val tagPatrons = arrayListOf<Patron>()
            val oneGuildPatrons = arrayListOf<Patron>()
            val guildPatrons = arrayListOf<Patron>()

            connManager.connection.createStatement().use { statement ->
                statement.executeQuery("SELECT * FROM patrons").use { resultSet ->
                    while (resultSet.next()) {
                        val guildId = if (resultSet.getLong("guild_id") == 0L) null else resultSet.getLong("guild_id")
                        val patron = Patron(
                            Patron.Type.valueOf(resultSet.getString("type").toUpperCase()),
                            resultSet.getLong("user_id"),
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
        runOnThread {
            connManager.connection.createStatement()
                .execute("DELETE FROM patrons WHERE user_id = $userId")
        }
    }

    override fun createOrUpdatePatron(patron: Patron) {
        runOnThread {
            connManager.connection.use { connection ->
                var id = 0

                // Check for an existing patron in the database and store the id
                connection.prepareStatement("SELECT id FROM patrons WHERE user_id = ? LIMIT 1").use { smt ->
                    smt.setLong(1, patron.userId)

                    smt.executeQuery().use { res ->
                        while (res.next()) {
                            id = res.getInt("id")
                        }
                    }
                }

                // Update the patron if we found an id
                if (id > 0) {
                    connection.prepareStatement("UPDATE patrons SET user_id = ?, guild_id = ?, type = ? WHERE id = ?").use { smt ->
                        smt.setLong(1, patron.userId)
                        smt.setString(2, patron.guildId?.toString())
                        smt.setString(3, patron.type.name)
                        smt.setInt(4, id)

                        smt.executeUpdate()
                        smt.closeOnCompletion()
                    }

                    return@runOnThread
                }

                // Create a patron if we didn't find one in the database
                connection.prepareStatement("INSERT INTO patrons (user_id, guild_id, type) VALUES (?, ?, ?)").use { smt ->
                    smt.setLong(1, patron.userId)
                    smt.setString(2, patron.guildId?.toString())
                    smt.setString(3, patron.type.name)

                    smt.executeUpdate()
                    smt.closeOnCompletion()
                }
            }
        }
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        runOnThread {
            // language=SQLite
            connManager.connection.prepareStatement(
                "INSERT INTO oneGuildPatrons(user_id, guild_id) VALUES( ? , ? ) ON CONFLICT(guild_id) DO UPDATE SET guild_id = ?"
            ).use {
                it.setLong(1, userId)
                it.setLong(2, guildId)
                it.setLong(3, guildId)

                it.executeUpdate()
                it.closeOnCompletion()
            }

            callback.invoke(userId, guildId)
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        val map = TLongLongHashMap()

        runOnThread {
            val statement = connManager.connection.prepareStatement(
                "SELECT * FROM oneGuildPatrons WHERE user_id = ? LIMIT 1"
            ).use {
                it.setLong(1, userId)
                it.closeOnCompletion()

                return@use it
            }

            val resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val guildId = resultSet.getLong("guild_id")

                map.put(userId, guildId)
            }

            callback.invoke(map)
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        runOnThread {
            connManager.connection.prepareStatement(
                // language=SQLite
                """
                    INSERT INTO bans(modUserId, Username, discriminator, userId, ban_date, unban_date, guildId)
                    VALUES(? , ? , ? , ? , current_date , ?, ?)
                """.trimIndent()
            ).use {
                it.setString(1, modId.toString())
                it.setString(2, userName)
                it.setString(3, userDiscriminator)
                it.setString(4, userId.toString())
                it.setString(5, unbanDate)
                it.setString(6, guildId.toString())
                it.execute()
                it.closeOnCompletion()
            }
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        runOnThread {
            connManager.connection.prepareStatement(
                // language=SQLite
                """
                    INSERT INTO warnings(mod_id, user_id, reason, guild_id, warn_date, expire_date)
                    VALUES(? , ? , ? , ?  , current_date, date(current_date, '+3 day') )
                """.trimIndent()).use {
                it.setString(1, modId.toString())
                it.setString(2, userId.toString())
                it.setString(3, reason)
                it.setString(4, guildId.toString())
                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long, callback: (Mute?) -> Unit) {
        runOnThread {
            var oldMute: Mute? = null

            // language=SQLite
            val res1 = connManager.connection.prepareStatement("""
                SELECT id FROM mutes WHERE guild_id = ? AND user_id = ?
            """.trimIndent())
                .use {
                    it.setString(1, guildId.toString())
                    it.setString(2, userId.toString())

                    it.executeQuery()
                }

            res1.use { res ->
                if (res.next()) {
                    oldMute = Mute(
                        res.getInt("id"),
                        res.getString("mod_id"),
                        res.getString("user_id"),
                        res.getString("user_tag"),
                        res.getString("guild_id")
                    )
                }
            }

            connManager.connection
                // language=SQLite
                .prepareStatement("""
                    INSERT INTO mutes(guild_id, mod_id, user_id, user_tag, unmute_date)
                    VALUES(? , ? , ? , ? , ?)
                """.trimIndent())
                .use {
                    it.setString(1, guildId.toString())
                    it.setString(2, modId.toString())
                    it.setString(3, userId.toString())
                    it.setString(4, userTag)
                    it.setDate(5, unmuteDate.toDate())

                    it.execute()
                    it.closeOnCompletion()
                }

            callback(oldMute)
        }
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit) {
        runOnThread {
            val conn = connManager.connection

            val smt = conn.prepareStatement(
                // language=SQLite
                "SELECT * FROM warnings WHERE user_id=? AND guild_id=? ORDER BY id DESC LIMIT 1"
            ).use {
                it.setString(1, userId.toString())
                it.setString(2, guildId.toString())

                return@use it
            }

            val result = smt.executeQuery()
            var ret: Warning? = null

            while (result.next()) {
                ret = Warning(
                    result.getInt("id"),
                    result.getString("warn_date"),
                    result.getString("mod_id"),
                    result.getString("reason"),
                    result.getString("guild_id")
                )
            }

            if (ret != null) {
                conn.prepareStatement(
                    // language=SQLite
                    "DELETE FROM warnings WHERE id=?"
                ).use {
                    it.setInt(1, ret.id)
                    it.executeUpdate()
                    it.closeOnCompletion()
                }
            }

            smt.close()
            result.close()
            callback.invoke(ret)
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        runOnThread {
            val warnings = ArrayList<Warning>()
            val smt = connManager.connection.prepareStatement(
                // language=SQLite
                "SELECT * FROM `warnings` WHERE user_id=? AND guild_id=? AND (DATE('now') <= DATE(expire_date, '+3 day'))"
            ).use {
                it.setString(1, userId.toString())
                it.setString(2, guildId.toString())
                it.closeOnCompletion()
                return@use it
            }

            val result = smt.executeQuery()

            while (result.next()) {
                warnings.add(Warning(
                    result.getInt("id"),
                    result.getString("warn_date"),
                    result.getString("mod_id"),
                    result.getString("reason"),
                    result.getString("guild_id")
                ))
            }

            callback.invoke(warnings)
        }
    }

    override fun purgeBans(ids: List<Int>) {
        runOnThread {
            val idsString = ids.joinToString(separator = ", ")
            connManager.connection.createStatement().use {
                it.execute("DELETE FROM bans WHERE id in ($idsString)")
                it.closeOnCompletion()
            }
        }
    }

    override fun getExpiredBansAndMutes(callback: (List<Ban>, List<Mute>) -> Unit) {
        runOnThread {
            val mutes = arrayListOf<Mute>()
            val bans = arrayListOf<Ban>()

            val muteSmt = connManager.connection.createStatement()
            val banSmt = connManager.connection.createStatement()

            muteSmt.closeOnCompletion()
            banSmt.closeOnCompletion()

            muteSmt.executeQuery("SELECT * FROM mutes WHERE unmute_date <= CURRENT_TIMESTAMP")
                .use {
                    while (it.next()) {
                        mutes.add(Mute(
                            it.getInt("id"),
                            it.getString("mod_id"),
                            it.getString("user_id"),
                            it.getString("user_tag"),
                            it.getString("guild_id")
                        ))
                    }
                    it.close()
                }

            banSmt.executeQuery("SELECT * FROM bans WHERE unban_date <= CURRENT_TIMESTAMP")
                .use {
                    while (it.next()) {
                        bans.add(Ban(
                            it.getInt("id"),
                            it.getString("modUserId"),
                            it.getString("userId"),
                            it.getString("Username"),
                            it.getString("discriminator"),
                            it.getString("guildId")
                        ))
                    }
                    it.close()
                }

            callback(bans, mutes)
        }
    }

    override fun purgeMutes(ids: List<Int>) {
        runOnThread {
            val idsString = ids.joinToString(separator = ", ")
            connManager.connection.createStatement().use {
                it.execute("DELETE FROM mutes WHERE id in ($idsString)")
                it.closeOnCompletion()
            }
        }
    }

    override fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit) {
        runOnThread {
            val items = ArrayList<VcAutoRole>()
            // language=SQLite
            val result = connManager.connection.createStatement().executeQuery("SELECT * FROM `vcAutoRoles`")

            while (result.next()) {
                items.add(VcAutoRole(
                    result.getLong("guild_id"),
                    result.getLong("voice_channel_id"),
                    result.getLong("role_id")
                ))
            }

            callback.invoke(items)
        }
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        runOnThread {
            connManager.connection.prepareStatement(
                // language=SQLite
                "INSERT INTO vcAutoRoles(guild_id, voice_channel_id, role_id) VALUES(? , ? , ?)"
            ).use {
                it.setString(1, guildId.toString())
                it.setString(2, voiceChannelId.toString())
                it.setString(3, roleId.toString())
                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun setVcAutoRoleBatch(guildId: Long, voiceChannelIds: List<Long>, roleId: Long) {
        voiceChannelIds.forEach {
            setVcAutoRole(guildId, it, roleId)
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        runOnThread {
            connManager.connection.prepareStatement(
                // language=SQLite
                "DELETE FROM vcAutoRoles WHERE voice_channel_id = ?"
            ).use {
                it.setString(1, voiceChannelId.toString())
                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        runOnThread {
            connManager.connection.prepareStatement(
                // language=SQLite
                "DELETE FROM vcAutoRoles WHERE guild_id = ?"
            ).use {
                it.setString(1, guildId.toString())
                it.executeUpdate()
                it.closeOnCompletion()
            }
        }
    }

    override fun loadTags(callback: (List<Tag>) -> Unit) {
        runOnThread {
            val tags = arrayListOf<Tag>()
            // language=SQLite
            val res = connManager.connection.createStatement().executeQuery("SELECT * FROM tags")

            while (res.next()) {
                val tag = Tag()

                tag.owner_id = res.getLong("owner_id")
                tag.name = res.getString("name")
                tag.content = res.getString("content")

                tags.add(tag)
            }

            res.close()

            callback.invoke(tags)
        }
    }

    override fun createTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            // language=SQLite
            val smt = connManager.connection.prepareStatement("INSERT INTO tags(owner_id, name, content) VALUES(?, ?, ?)")
            smt.setLong(1, tag.owner_id)
            smt.setString(2, tag.name)
            smt.setString(3, tag.content)

            smt.executeUpdate()
            smt.closeOnCompletion()

            callback.invoke(true, "")
        }
    }

    override fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        runOnThread {
            // language=SQLite
            val smt = connManager.connection.prepareStatement("DELETE FROM tags where name = ?")
            smt.setString(1, tag.name)
            smt.executeUpdate()
            smt.closeOnCompletion()

            callback.invoke(true, "")
        }
    }

    override fun createReminder(userId: Long, reminder: String, expireDate: Instant, channelId: Long, callback: (Boolean, Int) -> Unit) {
        runOnThread {
            val sql = if (channelId > 0) {
                // language=SQLite
                "INSERT INTO reminders(user_id, reminder, remind_date, remind_create_date, channel_id) VALUES (? , ? , ? , ? , ?)"
            } else {
                // language=SQLite
                "INSERT INTO reminders(user_id, reminder, remind_date, remind_create_date) VALUES (? , ? , ?, ?)"
            }

            val smt = connManager.connection.prepareStatement(sql)

            smt.setString(1, userId.toString())
            smt.setString(2, reminder)
            smt.setDate(3, expireDate.toSQL())
            smt.setDate(4, java.sql.Date(System.currentTimeMillis()))

            if (channelId > 0) {
                smt.setString(5, channelId.toString())
            }

            try {
                smt.execute()
                callback.invoke(true, smt.generatedKeys.getInt(1))
            } catch (e: SQLException) {
                e.printStackTrace()
                callback.invoke(false, -1)
            }
        }
    }

    override fun removeReminder(reminderId: Int, userId: Long, callback: (Boolean) -> Unit) {
        runOnThread({
            val smt = connManager.connection.createStatement()

            // language=SQLite
            smt.execute("DELETE FROM reminders WHERE id = $reminderId AND user_id = $userId")
            smt.closeOnCompletion()

            callback.invoke(true)
        }, {
            callback.invoke(false)
        })
    }

    override fun showReminder(reminderId: Int, userId: Long, callback: (Reminder?) -> Unit) {
        runOnThread {
            connManager.connection.prepareStatement("SELECT * FROM reminders WHERE id = ? AND user_id = ? LIMIT 1").use {
                it.setInt(1, reminderId)
                it.setLong(2, userId)

                it.executeQuery().use results@ { result ->
                    if (!result.next()) {
                        callback(null)
                        return@results
                    }

                    callback(Reminder(
                        result.getInt("id"),
                        result.getLong("user_id"),
                        result.getString("reminder"),
                        result.getDate("remind_create_date").asInstant(),
                        result.getDate("remind_date").asInstant(),
                        result.getLong("channel_id")
                    ))
                }

                it.closeOnCompletion()
            }
        }
    }

    override fun listReminders(userId: Long, callback: (List<Reminder>) -> Unit) {
        runOnThread {
            val reminders = arrayListOf<Reminder>()

            connManager.connection.prepareStatement("SELECT * FROM reminders WHERE user_id = ?").use {
                it.setLong(1, userId)

                it.executeQuery().use { result ->
                    while (result.next()) {
                        reminders.add(Reminder(
                            result.getInt("id"),
                            result.getLong("user_id"),
                            result.getString("reminder"),
                            result.getDate("remind_create_date").asInstant(),
                            result.getDate("remind_date").asInstant(),
                            result.getLong("channel_id")
                        ))
                    }

                    it.closeOnCompletion()

                    callback(reminders)
                }
            }
        }
    }

    override fun purgeReminders(ids: List<Int>) {
        runOnThread {
            val inClause = '(' + ids.joinToString() + ')'
            //language=SQLite
            val sql = "DELETE FROM reminders WHERE id IN $inClause"
            val smt = connManager.connection.createStatement()

            smt.execute(sql)
            smt.closeOnCompletion()
        }
    }

    override fun getExpiredReminders(callback: (List<Reminder>) -> Unit) {
        runOnThread {
            val reminders = ArrayList<Reminder>()
            val smt = connManager.connection.prepareStatement(
                // language=SQLite
                "SELECT * FROM `reminders` WHERE (DATETIME('now') > DATETIME(remind_date / 1000, 'unixepoch'))")

            smt.closeOnCompletion()

            val result = smt.executeQuery()

            while (result.next()) {
                reminders.add(Reminder(
                    result.getInt("id"),
                    result.getLong("user_id"),
                    result.getString("reminder"),
                    result.getDate("remind_create_date").asInstant(),
                    result.getDate("remind_date").asInstant(),
                    result.getLong("channel_id")
                ))
            }

            if (reminders.isNotEmpty()) {
                callback.invoke(reminders)
            }
        }
    }

    override fun setWarnActions(guildId: Long, actions: List<WarnAction>) {
        runOnThread {
            // clear all warn actions
            connManager.connection.createStatement().use {
                it.execute("DELETE FROM warn_actions WHERE guild_id = '$guildId'")
            }

            connManager.connection.prepareStatement(
                "INSERT INTO warn_actions (guild_id, duration, threshold, type) VALUES ${actions.indices.joinToString { " (? ,? ,? ,?)" }};"
            ).use { smt ->
                var loopIndex = 1

                for (item in actions) {
                    val firstIndex = 1 * loopIndex

                    smt.setString(firstIndex, guildId.toString())
                    smt.setInt(firstIndex + 1, item.duration)
                    smt.setInt(firstIndex + 2, item.threshold)
                    smt.setString(firstIndex + 3, item.type.id)

                    loopIndex += 4
                }

                smt.execute()
                smt.closeOnCompletion()
            }
        }
    }

    private fun changeCommand(guildId: Long, invoke: String, message: String, isEdit: Boolean, autoresponse: Boolean = false): Triple<Boolean, Boolean, Boolean>? {
        val sqlQuerry = if (isEdit) {
            //language=SQLite
            "UPDATE customCommands SET message = ? , autoresponse = ? WHERE guildId = ? AND invoke = ?"
        } else {
            //language=SQLite
            "INSERT INTO customCommands(guildId, invoke, message, autoresponse) VALUES (? , ? , ? , ?)"
        }
        val conn = connManager.connection

        conn.prepareStatement(sqlQuerry).use {
            it.setString(if (isEdit) 3 else 1, guildId.toString())
            it.setString(if (isEdit) 4 else 2, invoke)
            it.setString(if (isEdit) 1 else 3, message)
            it.setBoolean(if (isEdit) 2 else 4, autoresponse)
            it.execute()
            it.closeOnCompletion()
        }

        return null
    }

    private fun getBlackListsForGuild(guildId: Long): List<String> {
        val list = arrayListOf<String>()
        val smt = connManager.connection.createStatement()

        // language=SQLite
        val res = smt.executeQuery("SELECT * FROM blacklists WHERE guild_id = '$guildId'")

        while (res.next()) {
            list.add(res.getString("word"))
        }

        res.close()

        return list
    }

    private fun getWarnActionsForGuild(guildId: Long): List<WarnAction> {
        val list = arrayListOf<WarnAction>()

        connManager.connection.prepareStatement("SELECT * FROM warn_actions WHERE guild_id = ?").use { smt ->
            smt.setString(1, guildId.toString())

            smt.executeQuery().use { res ->
                while(res.next()) {
                    list.add(WarnAction(
                        WarnAction.Type.valueOf(res.getString("type")),
                        res.getInt("threshold"),
                        res.getInt("duration")
                    ))
                }
            }
        }


        return list
    }

    private fun TemporalAccessor.toSQL() = java.sql.Date(Instant.from(this).toEpochMilli())
    private fun java.sql.Date.asInstant() = Instant.ofEpochMilli(this.time)
    private fun String.toDate() = fromDatabaseFormat(this).toSQL()

    private fun ResultSet.toGuildSettings(guildId: Long): GuildSettings {
        val blackList = getBlackListsForGuild(guildId)
        val warnActions = getWarnActionsForGuild(guildId)

        return GuildSettings(guildId)
            .setEnableJoinMessage(this.getBoolean("enableJoinMessage"))
            .setEnableSwearFilter(this.getBoolean("enableSwearFilter"))
            .setCustomJoinMessage(replaceNewLines(this.getString("customWelcomeMessage")))
            .setCustomPrefix(this.getString("prefix"))
            .setLogChannel(toLong(this.getString("logChannelId")))
            .setWelcomeLeaveChannel(toLong(this.getString("welcomeLeaveChannel")))
            .setCustomLeaveMessage(replaceNewLines(this.getString("customLeaveMessage")))
            .setAutoroleRole(toLong(this.getString("autoRole")))
            .setServerDesc(replaceNewLines(this.getString("serverDesc")))
            .setAnnounceTracks(this.getBoolean("announceNextTrack"))
            .setAutoDeHoist(this.getBoolean("autoDeHoist"))
            .setFilterInvites(this.getBoolean("filterInvites"))
            .setEnableSpamFilter(this.getBoolean("spamFilterState"))
            .setMuteRoleId(toLong(this.getString("muteRoleId")))
            .setRatelimits(ratelimmitChecks(this.getString("ratelimits")))
            .setKickState(this.getBoolean("kickInsteadState"))
            .setLeaveTimeout(this.getInt("leave_timeout"))
            .setSpamThreshold(this.getInt("spam_threshold"))
            .setBanLogging(this.getBoolean("logBan"))
            .setUnbanLogging(this.getBoolean("logUnban"))
            .setKickLogging(this.getBoolean("logKick"))
            .setMuteLogging(this.getBoolean("logMute"))
            .setWarnLogging(this.getBoolean("logWarn"))
            .setFilterType(this.getString("profanity_type"))
            .setAiSensitivity(this.getFloat("aiSensitivity"))
            .setAllowAllToStop(this.getBoolean("allow_all_to_stop"))
            .setBlacklistedWords(blackList)
            .setWarnActions(warnActions)
    }
}
