/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.connections.database.SQLiteDatabaseConnectionManager
import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.VcAutoRole
import ml.duncte123.skybot.objects.api.Warning
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.GuildSettingsUtils.*
import org.apache.http.MethodNotSupportedException
import java.io.File
import java.sql.SQLException
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class SqliteDatabaseAdapter(variables: Variables) : DatabaseAdapter(variables) {
    private val connManager = SQLiteDatabaseConnectionManager(File("database.db"))

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        variables.database.run {
            val customCommands = arrayListOf<CustomCommand>()

            connManager.use { manager ->

                val con = manager.connection

                val res = con.createStatement().executeQuery("SELECT * FROM customCommands")

                while (res.next()) {
                    customCommands.add(CustomCommandImpl(
                        res.getString("invoke"),
                        res.getString("message"),
                        res.getLong("guildId"),
                        res.getBoolean("autoresponse")
                    ))
                }
            }

            callback.invoke(customCommands)
        }
    }

    override fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            val res = changeCommand(guildId, invoke, message, false)

            callback.invoke(res)
        }
    }

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            val res = changeCommand(guildId, invoke, message, true)

            callback.invoke(res)
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Any?) {
        variables.database.run {

            connManager.use { manager ->
                val con = manager.connection

                con.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?").apply {
                    setString(1, invoke)
                    setString(2, guildId.toString())
                    execute()
                }
            }

            callback.invoke(true)
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        variables.database.run {

            val settings = arrayListOf<GuildSettings>()

            connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.createStatement()

                val res = smt.executeQuery("SELECT * FROM guildSettings")

                while (res.next()) {
                    val guildId = toLong(res.getString("guildId"))

                    val blackList = getBlackListsForGuild(guildId)

                    settings.add(GuildSettings(guildId)
                        .setEnableJoinMessage(res.getBoolean("enableJoinMessage"))
                        .setEnableSwearFilter(res.getBoolean("enableSwearFilter"))
                        .setCustomJoinMessage(replaceNewLines(res.getString("customWelcomeMessage")))
                        .setCustomPrefix(res.getString("prefix"))
                        .setLogChannel(toLong(res.getString("logChannelId")))
                        .setWelcomeLeaveChannel(toLong(res.getString("welcomeLeaveChannel")))
                        .setCustomLeaveMessage(replaceNewLines(res.getString("customLeaveMessage")))
                        .setAutoroleRole(toLong(res.getString("autoRole")))
                        .setServerDesc(replaceNewLines(res.getString("serverDesc")))
                        .setAnnounceTracks(res.getBoolean("announceNextTrack"))
                        .setAutoDeHoist(res.getBoolean("autoDeHoist"))
                        .setFilterInvites(res.getBoolean("filterInvites"))
                        .setEnableSpamFilter(res.getBoolean("spamFilterState"))
                        .setMuteRoleId(toLong(res.getString("muteRoleId")))
                        .setRatelimits(ratelimmitChecks(res.getString("ratelimits")))
                        .setKickState(res.getBoolean("kickInsteadState"))
                        .setLeaveTimeout(res.getInt("leave_timeout"))
                        .setSpamThreshold(res.getInt("spam_threshold"))
                        .setBlacklistedWords(blackList)
                    )
                }
                callback.invoke(settings)
            }
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.prepareStatement("INSERT INTO blacklists(guild_id, word) VALUES( ? , ? )").apply {

                    setString(1, guildId.toString())
                    setString(2, word)

                    executeUpdate()
                }
            }
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.prepareStatement("DELETE FROM blacklists WHERE guild_id = ? AND word = ?").apply {

                    setString(1, guildId.toString())
                    setString(2, word)

                    executeUpdate()
                }
            }
        }
    }

    override fun clearBlacklist(guildId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.createStatement().executeQuery("DELETE FROM blacklists where guild_id = '$guildId'")
            }
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit) {
        throw MethodNotSupportedException("Not supported for SQLite")
    }

    override fun deleteGuildSetting(guildId: Long) {
        variables.database.run {

            connManager.use { manager ->
                val connection = manager.connection

                connection.createStatement().executeQuery("DELETE FROM guildSettings where guildId = '$guildId'")
            }
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.prepareStatement("UPDATE guildSettings SET " +
                    "enableJoinMessage= ? , " +
                    "enableSwearFilter= ? ," +
                    "customWelcomeMessage= ? ," +
                    "prefix= ? ," +
                    "autoRole= ? ," +
                    "logChannelId= ? ," +
                    "welcomeLeaveChannel= ? ," +
                    "customLeaveMessage = ? ," +
                    "serverDesc = ? ," +
                    "announceNextTrack = ? ," +
                    "autoDeHoist = ? ," +
                    "filterInvites = ? ," +
                    "spamFilterState = ? ," +
                    "muteRoleId = ? ," +
                    "ratelimits = ? ," +
                    "kickInsteadState = ? ," +
                    "leave_timeout = ? ," +
                    "spam_threshold = ? " +
                    "WHERE guildId='${guildSettings.guildId}'"
                ).apply {
                    setBoolean(1, guildSettings.isEnableJoinMessage)
                    setBoolean(2, guildSettings.isEnableSwearFilter)
                    setString(3, fixUnicodeAndLines(guildSettings.customJoinMessage))
                    setString(4, replaceUnicode(guildSettings.customPrefix))
                    setString(5, guildSettings.autoroleRole.toString())
                    setString(6, guildSettings.logChannel.toString())
                    setString(7, guildSettings.welcomeLeaveChannel.toString())
                    setString(8, fixUnicodeAndLines(guildSettings.customLeaveMessage))
                    setString(9, fixUnicodeAndLines(guildSettings.serverDesc))
                    setBoolean(10, guildSettings.isAnnounceTracks)
                    setBoolean(11, guildSettings.isAutoDeHoist)
                    setBoolean(12, guildSettings.isFilterInvites)
                    setBoolean(13, guildSettings.isEnableSpamFilter)
                    setString(14, guildSettings.muteRoleId.toString())
                    setString(15, convertJ2S(guildSettings.ratelimits))
                    setBoolean(16, guildSettings.kickState)
                    setInt(17, guildSettings.leaveTimeout)
                    setInt(18, guildSettings.spamThreshold)
                    executeUpdate()
                }
            }

            callback.invoke(true)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        variables.database.run {
            val guildId = guildSettings.guildId

            connManager.use { manager ->
                val connection = manager.connection
                val resultSet = connection.createStatement()
                    .executeQuery("SELECT id FROM guildSettings WHERE guildId='$guildId'")

                var rows = 0

                while (resultSet.next()) {
                    rows++
                }

                if (rows == 0) {
                    val smt = connection.prepareStatement("INSERT INTO guildSettings(guildId," +
                        "customWelcomeMessage, prefix, customLeaveMessage, ratelimits) " +
                        "VALUES('$guildId' , ? , ? , ? , ?)")

                    smt.setString(1, guildSettings.customJoinMessage)
                    smt.setString(2, Settings.PREFIX)
                    smt.setString(3, guildSettings.customLeaveMessage.replace("\\P{Print}".toRegex(), ""))
                    smt.setString(4, "20|45|60|120|240|2400".replace("\\P{Print}".toRegex(), ""))
                    smt.execute()
                }
            }

            callback.invoke(true)
        }
    }

    override fun loadEmbedSettings(callback: (TLongIntMap) -> Unit) {
        variables.database.run {
            val map = TLongIntHashMap()

            connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.createStatement()

                val res = smt.executeQuery("SELECT * FROM embedSettings")

                while (res.next()) {
                    map.put(res.getLong("guild_id"), res.getInt("embed_color"))
                }

                callback.invoke(map)
            }
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.prepareStatement(
                    "INSERT INTO embedSettings(guild_id, embed_color) VALUES( ? , ? ) ON CONFLICT(guild_id) DO UPDATE SET embed_color = ?").apply {

                    setString(1, guildId.toString())
                    setInt(2, color)
                    setInt(3, color)

                    executeUpdate()
                }
            }
        }
    }

    override fun loadOneGuildPatrons(callback: (TLongLongMap) -> Unit) {
        variables.database.run {
            val map = TLongLongHashMap()

            connManager.use { manager ->
                val connection = manager.connection

                val resultSet = connection.createStatement().executeQuery("SELECT * FROM oneGuildPatrons")

                while (resultSet.next()) {
                    map.put(resultSet.getLong("user_id"), resultSet.getLong("guild_id"))
                }

                callback.invoke(map)
            }
        }
    }

    override fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.prepareStatement("INSERT INTO oneGuildPatrons" +
                    "(user_id, guild_id) VALUES( ? , ? ) ON DUPLICATE KEY UPDATE guild_id = ?").apply {

                    setLong(1, userId)
                    setLong(2, guildId)
                    setLong(3, guildId)

                    executeUpdate()
                }
            }

            callback.invoke(userId, guildId)
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        val map = TLongLongHashMap()

        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                val statement = connection.prepareStatement(
                    "SELECT * FROM oneGuildPatrons WHERE user_id = ? LIMIT 1"
                ).apply {
                    setLong(1, userId)
                }

                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val guildId = resultSet.getLong("guild_id")

                    map.put(userId, guildId)
                }
            }

            callback.invoke(map)
        }
    }

    override fun removeOneGuildPatron(userId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val connection = manager.connection

                connection.createStatement()
                    .execute("DELETE FROM oneGuildPatrons WHERE user_id = $userId")
            }
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val conn = manager.connection

                conn.prepareStatement(
                    "INSERT INTO bans(modUserId, Username, discriminator, userId, ban_date, unban_date, guildId) " +
                        "VALUES(? , ? , ? , ? , NOW() , ?, ?)"
                ).apply {

                    setString(1, modId.toString())
                    setString(2, userName)
                    setString(3, userDiscriminator)
                    setString(4, userId.toString())
                    setString(5, unbanDate)
                    setString(6, guildId.toString())
                    execute()
                }
            }
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        variables.database.run {
            connManager.use { manager ->
                val conn = manager.connection

                conn.prepareStatement(
                    "INSERT INTO warnings(mod_id, user_id, reason, guild_id, warn_date, expire_date) " +
                        "VALUES(? , ? , ? , ?  , current_date, date(current_date, '+3 day') )").apply {

                    setString(1, modId.toString())
                    setString(2, userId.toString())
                    setString(3, reason)
                    setString(4, guildId.toString())
                    executeUpdate()
                }
            }
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long) {
        // Api only
    }

    override fun deleteLatestWarningForUser(userId: Long, guildId: Long, callback: (Warning?) -> Unit) {
        variables.database.run {
            connManager.use {
                val conn = it.connection

                val smt = conn.prepareStatement(
                    "SELECT * FROM warnings WHERE user_id=? AND guild_id=? ORDER BY id DESC LIMIT 1"
                ).apply {
                    setString(1, userId.toString())
                    setString(2, guildId.toString())
                }

                val result = smt.executeQuery()
                var ret: Warning? = null

                while (result.next()) {
                    ret = Warning(
                        result.getInt("id"),
                        result.getString("mod_id"),
                        result.getString("reason"),
                        result.getString("guild_id")
                    )
                }

                if (ret != null) {
                    conn.prepareStatement(
                        "DELETE FROM warnings WHERE id=?"
                    ).apply {
                        setInt(1, ret.id)
                        executeUpdate()
                        closeOnCompletion()
                    }
                }

                smt.close()
                result.close()
                callback.invoke(ret)
            }
        }
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        variables.database.run {
            val warnings = ArrayList<Warning>()

            connManager.use {
                val conn = it.connection

                val smt = conn.prepareStatement(
                    "SELECT * FROM `warnings` WHERE user_id=? AND guild_id=? AND (DATE('now') <= DATE(expire_date, '+3 day'))"
                ).apply {
                    setString(1, userId.toString())
                    setString(2, guildId.toString())
                }

                val result = smt.executeQuery()

                while (result.next()) {
                    warnings.add(Warning(
                        result.getInt("id"),
                        result.getString("mod_id"),
                        result.getString("reason"),
                        result.getString("guild_id")
                    ))
                }
            }

            callback.invoke(warnings)
        }
    }

    override fun purgeBans(ids: List<Int>) {
        // Api only
    }

    override fun getExpiredBansAndMutes(callback: (Pair<List<Ban>, List<Mute>>) -> Unit) {
        // Api only
    }

    override fun purgeMutes(ids: List<Int>) {
        // Api only
    }

    override fun getVcAutoRoles(callback: (List<VcAutoRole>) -> Unit) {
        variables.database.run {
            val items = ArrayList<VcAutoRole>()

            connManager.use {
                val conn = it.connection

                val result = conn.createStatement().executeQuery("SELECT * FROM `vcAutoRoles`")

                while (result.next()) {
                    items.add(VcAutoRole(
                        result.getLong("guild_id"),
                        result.getLong("voice_channel_id"),
                        result.getLong("role_id")
                    ))
                }
            }

            callback.invoke(items)
        }
    }

    override fun setVcAutoRole(guildId: Long, voiceChannelId: Long, roleId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val conn = manager.connection

                conn.prepareStatement(
                    "INSERT INTO vcAutoRoles(guild_id, voice_channel_id, role_id) VALUES(? , ? , ?)"
                ).apply {

                    setString(1, guildId.toString())
                    setString(2, voiceChannelId.toString())
                    setString(3, roleId.toString())
                    executeUpdate()
                }
            }
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val conn = manager.connection

                conn.prepareStatement(
                    "DELETE FROM vcAutoRoles WHERE voice_channel_id = ?"
                ).apply {

                    setString(1, voiceChannelId.toString())
                    executeUpdate()
                }
            }
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        variables.database.run {
            connManager.use { manager ->
                val conn = manager.connection

                conn.prepareStatement(
                    "DELETE FROM vcAutoRoles WHERE guild_id = ?"
                ).apply {
                    setString(1, guildId.toString())
                    executeUpdate()
                }
            }
        }
    }

    override fun loadTags(callback: (List<Tag>) -> Unit) {
        variables.database.run {
            val tags = arrayListOf<Tag>()

            connManager.use {
                val conn = it.connection

                val res = conn.createStatement().executeQuery("SELECT * FROM tags")

                while(res.next()) {
                    val tag = Tag()

                    tag.owner_id = res.getLong("owner_id")
                    tag.name = res.getString("name")
                    tag.content = res.getString("content")

                    tags.add(tag)
                }

                res.close()
            }

            callback.invoke(tags)
        }
    }

    override fun createTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        variables.database.run {
            connManager.use {
                val conn = it.connection

                try {
                    val smt = conn.prepareStatement("INSERT INTO tags(owner_id, name, content) VALUES(?, ?, ?)")
                    smt.setLong(1, tag.owner_id)
                    smt.setString(2, tag.name)
                    smt.setString(3, tag.content)

                    smt.executeUpdate()
                    smt.close()

                    callback.invoke(true, "")
                } catch (e: SQLException) {
                    callback.invoke(false, e.message ?: "Unknown reason")
                }
            }
        }
    }

    override fun deleteTag(tag: Tag, callback: (Boolean, String) -> Unit) {
        variables.database.run {
            connManager.use {
                val conn = it.connection

                try {
                    val smt = conn.prepareStatement("DELETE FROM tags where name = ?")
                    smt.setString(1, tag.name)
                    smt.executeUpdate()
                    smt.close()

                    callback.invoke(true, "")
                } catch (e: SQLException) {
                    callback.invoke(false, e.message ?: "Unknown reason")
                }
            }
        }
    }

    private fun changeCommand(guildId: Long, invoke: String, message: String, isEdit: Boolean, autoresponse: Boolean = false): Triple<Boolean, Boolean, Boolean>? {
        val sqlQuerry = if (isEdit) {
            "UPDATE customCommands SET message = ? , autoresponse = ? WHERE guildId = ? AND invoke = ?"
        } else {
            "INSERT INTO customCommands(guildId, invoke, message, autoresponse) VALUES (? , ? , ? , ?)"
        }

        connManager.use { manager ->
            val conn = manager.connection

            conn.prepareStatement(sqlQuerry).apply {

                setString(if (isEdit) 3 else 1, guildId.toString())
                setString(if (isEdit) 4 else 2, invoke)
                setString(if (isEdit) 1 else 3, message)
                setBoolean(if (isEdit) 2 else 4, autoresponse)
                execute()
            }
        }

        return null
    }

    private fun getBlackListsForGuild(guildId: Long): List<String> {
        val list = arrayListOf<String>()

        connManager.use { manager ->
            val connection = manager.connection
            val smt = connection.createStatement()

            val res = smt.executeQuery("SELECT * FROM blacklists WHERE guild_id = '$guildId'")

            while (res.next()) {
                list.add(res.getString("word"))
            }

            res.close()
        }

        return list
    }
}
