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
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.VcAutoRole
import ml.duncte123.skybot.objects.api.Warning
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.GuildSettingsUtils.*
import org.apache.http.MethodNotSupportedException
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
class SqliteDatabaseAdapter(variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        val database = variables.database

        database.run {
            val customCommands = arrayListOf<CustomCommand>()

            database.connManager.use { manager ->

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

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit) {
        val database = variables.database

        database.run {

            database.connManager.use { manager ->
                val con = manager.connection

                val stm = con.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?")
                stm.setString(1, invoke)
                stm.setString(2, guildId.toString())
                stm.execute()
            }

            callback.invoke(true)
        }
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        val database = variables.database

        database.run {

            val dbName = database.name
            val settings = arrayListOf<GuildSettings>()
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.createStatement()

                val res = smt.executeQuery("SELECT * FROM $dbName.guildSettings")

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
                        .setBlacklistedWords(blackList)
                    )
                }
                callback.invoke(settings)
            }
        }
    }

    override fun addWordToBlacklist(guildId: Long, word: String) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.prepareStatement("INSERT INTO blacklists(guild_id, word) VALUES( ? , ? )")

                smt.setString(1, guildId.toString())
                smt.setString(2, word)

                smt.executeUpdate()
            }
        }
    }

    override fun removeWordFromBlacklist(guildId: Long, word: String) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.prepareStatement("DELETE FROM blacklists WHERE guild_id = ? AND word = ?")

                smt.setString(1, guildId.toString())
                smt.setString(2, word)

                smt.executeUpdate()
            }
        }
    }

    override fun clearBlacklist(guildId: Long) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.prepareStatement("DELETE FROM blacklists where guild_id = '$guildId'")

                smt.executeUpdate()
            }
        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit) {
        throw MethodNotSupportedException("Not supported for SQLite")
    }

    override fun deleteGuildSetting(guildId: Long) {
        val database = variables.database

        database.run {

            database.connManager.use { manager ->
                val connection = manager.connection

                val smt = connection.prepareStatement("DELETE FROM guildSettings where guildId = '$guildId'")
                smt.executeUpdate()

            }
        }
    }

    override fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        val database = variables.database

        database.run {

            val dbName = database.name

            database.connManager.use { manager ->
                val connection = manager.connection

                val smt = connection.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
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
                    "kickInsteadState = ? " +
                    "WHERE guildId='" + guildSettings.guildId + "'")
                smt.setBoolean(1, guildSettings.isEnableJoinMessage)
                smt.setBoolean(2, guildSettings.isEnableSwearFilter)
                smt.setString(3, fixUnicodeAndLines(guildSettings.customJoinMessage))
                smt.setString(4, replaceUnicode(guildSettings.customPrefix))
                smt.setString(5, guildSettings.autoroleRole.toString())
                smt.setString(6, guildSettings.logChannel.toString())
                smt.setString(7, guildSettings.welcomeLeaveChannel.toString())
                smt.setString(8, fixUnicodeAndLines(guildSettings.customLeaveMessage))
                smt.setString(9, fixUnicodeAndLines(guildSettings.serverDesc))
                smt.setBoolean(10, guildSettings.isAnnounceTracks)
                smt.setBoolean(11, guildSettings.isAutoDeHoist)
                smt.setBoolean(12, guildSettings.isFilterInvites)
                smt.setBoolean(13, guildSettings.isEnableSpamFilter)
                smt.setString(14, guildSettings.muteRoleId.toString())
                smt.setString(15, convertJ2S(guildSettings.ratelimits))
                smt.setBoolean(16, guildSettings.kickState)
                smt.executeUpdate()

            }

            callback.invoke(true)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        val database = variables.database

        database.run {

            val guildId = guildSettings.guildId

            database.connManager.use { manager ->
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
        val database = variables.database

        database.run {
            val map = TLongIntHashMap()

            val dbName = database.name
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.createStatement()

                val res = smt.executeQuery("SELECT * FROM $dbName.embedSettings")

                while (res.next()) {
                    map.put(res.getLong("guild_id"), res.getInt("embed_color"))
                }

                callback.invoke(map)
            }
        }
    }

    override fun updateOrCreateEmbedColor(guildId: Long, color: Int) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection
                val smt = connection.prepareStatement(
                    "INSERT INTO embedSettings(guild_id, embed_color) VALUES( ? , ? ) ON CONFLICT(guild_id) DO UPDATE SET embed_color = ?")

                smt.setString(1, guildId.toString())
                smt.setInt(2, color)
                smt.setInt(3, color)

                smt.executeUpdate()
            }
        }
    }

    override fun loadOneGuildPatrons(callback: (TLongLongMap) -> Unit) {
        val database = variables.database

        database.run {
            val map = TLongLongHashMap()

            database.connManager.use { manager ->
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
        val database = variables.database
        val dbName = database.name

        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection

                val smt = connection.prepareStatement("INSERT INTO $dbName.oneGuildPatrons" +
                    "(user_id, guild_id) VALUES( ? , ? ) ON DUPLICATE KEY UPDATE guild_id = ?")

                smt.setLong(1, userId)
                smt.setLong(2, guildId)
                smt.setLong(3, guildId)

                smt.executeUpdate()
            }

            callback.invoke(userId, guildId)
        }
    }

    override fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit) {
        val database = variables.database
        val map = TLongLongHashMap()

        database.run {

            database.connManager.use { manager ->
                val connection = manager.connection

                val statement = connection.prepareStatement(
                    "SELECT * FROM oneGuildPatrons WHERE user_id = ? LIMIT 1")

                statement.setLong(1, userId)

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
        val database = variables.database
        database.run {
            database.connManager.use { manager ->
                val connection = manager.connection

                connection.createStatement()
                    .execute("DELETE FROM oneGuildPatrons WHERE user_id = $userId")
            }
        }
    }

    override fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val conn = manager.connection
                val smt = conn.prepareStatement(
                    "INSERT INTO bans(modUserId, Username, discriminator, userId, ban_date, unban_date, guildId) " +
                        "VALUES(? , ? , ? , ? , NOW() , ?, ?)")

                smt.setString(1, modId.toString())
                smt.setString(2, userName)
                smt.setString(3, userDiscriminator)
                smt.setString(4, userId.toString())
                smt.setString(5, unbanDate)
                smt.setString(6, guildId.toString())
                smt.execute()
            }
        }
    }

    override fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val conn = manager.connection

                val smt = conn.prepareStatement(
                    "INSERT INTO warnings(mod_id, user_id, reason, guild_id, warn_date, expire_date) " +
                        "VALUES(? , ? , ? , ?  , CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY) )")

                smt.setString(1, modId.toString())
                smt.setString(2, userId.toString())
                smt.setString(3, reason)
                smt.setString(4, guildId.toString())
                smt.executeUpdate()
            }
        }
    }

    override fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long) {
        // Api only
    }

    override fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit) {
        val database = variables.database

        database.run {
            val warnings = ArrayList<Warning>()

            database.connManager.use {
                val conn = it.connection

                val smt = conn.prepareStatement(
                    "SELECT * FROM `warnings` WHERE user_id=? AND guild_id=? AND (CURRENT_DATE <= DATE(expire_date, '+3 day'))")
                smt.setString(1, userId.toString())
                smt.setString(2, guildId.toString())
                val result = smt.executeQuery()

                while (result.next()) {
                    warnings.add(Warning(
                        result.getInt("id"),
                        result.getDate("warn_date"),
                        result.getDate("expire_date"),
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
        val database = variables.database

        database.run {
            val items = ArrayList<VcAutoRole>()

            database.connManager.use {
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
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val conn = manager.connection

                val smt = conn.prepareStatement(
                    "INSERT INTO vcAutoRoles(guild_id, voice_channel_id, role_id) VALUES(? , ? , ?)"
                )

                smt.setString(1, guildId.toString())
                smt.setString(2, voiceChannelId.toString())
                smt.setString(3, roleId.toString())
                smt.executeUpdate()
            }
        }
    }

    override fun removeVcAutoRole(voiceChannelId: Long) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val conn = manager.connection

                val smt = conn.prepareStatement(
                    "DELETE FROM vcAutoRoles WHERE voice_channel_id = ?"
                )

                smt.setString(1, voiceChannelId.toString())
                smt.executeUpdate()
            }
        }
    }

    override fun removeVcAutoRoleForGuild(guildId: Long) {
        val database = variables.database

        database.run {
            database.connManager.use { manager ->
                val conn = manager.connection

                val smt = conn.prepareStatement(
                    "DELETE FROM vcAutoRoles WHERE guild_id = ?"
                )

                smt.setString(1, guildId.toString())
                smt.executeUpdate()
            }
        }
    }

    private fun changeCommand(guildId: Long, invoke: String, message: String, isEdit: Boolean, autoresponse: Boolean = false): Triple<Boolean, Boolean, Boolean>? {
        val database = variables.database

        val sqlQuerry = if (isEdit) {
            "UPDATE customCommands SET message = ? , autoresponse = ? WHERE guildId = ? AND invoke = ?"
        } else {
            "INSERT INTO customCommands(guildId, invoke, message, autoresponse) VALUES (? , ? , ? , ?)"
        }
        database.connManager.use { manager ->
            val conn = manager.connection

            val stm = conn.prepareStatement(sqlQuerry)

            stm.setString(if (isEdit) 3 else 1, guildId.toString())
            stm.setString(if (isEdit) 4 else 2, invoke)
            stm.setString(if (isEdit) 1 else 3, message)
            stm.setBoolean(if (isEdit) 2 else 4, autoresponse)
            stm.execute()
        }

        return null
    }

    private fun getBlackListsForGuild(guildId: Long): List<String> {
        val database = variables.database
        val list = arrayListOf<String>()

        database.connManager.use { manager ->
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
