/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.command.custom.CustomCommandImpl
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.GuildSettingsUtils.*
import org.apache.http.MethodNotSupportedException
import java.sql.SQLException

class SqliteDatabaseAdapter(private val variables: Variables) : DatabaseAdapter(variables) {

    override fun getCustomCommands(callback: (List<CustomCommand>) -> Unit) {
        val database = variables.database

        database.run {
            val customCommands = arrayListOf<CustomCommand>()

            try {
                database.connManager.use { manager ->

                    val con = manager.connection

                    val res = con.createStatement().executeQuery("SELECT invoke, message, guildId FROM customCommands")
                    while (res.next()) {
                        customCommands.add(CustomCommandImpl(
                            res.getString("invoke"),
                            res.getString("message"),
                            res.getLong("guildId")
                        ))
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
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

    override fun updateCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit) {
        variables.database.run {
            val res = changeCommand(guildId, invoke, message, true)

            callback.invoke(res)
        }
    }

    override fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit) {
        val database = variables.database

        val ret = database.run<Boolean> {

            try {
                database.connManager.use { manager ->
                    val con = manager.connection

                    val stm = con.prepareStatement("DELETE FROM customCommands WHERE invoke = ? AND guildId = ?")
                    stm.setString(1, invoke)
                    stm.setString(2, guildId.toString())
                    stm.execute()
                }

            } catch (e: SQLException) {
                e.printStackTrace()
                return@run false
            }

            return@run true
        }.get()

        callback.invoke(ret)
    }

    override fun getGuildSettings(callback: (List<GuildSettings>) -> Unit) {
        val database = variables.database

        database.run {

            val dbName = database.name
            database.run {
                try {
                    val settings = arrayListOf<GuildSettings>()
                    database.connManager.use { manager ->
                        val connection = manager.connection
                        val smt = connection.createStatement()

                        val res = smt.executeQuery("SELECT * FROM $dbName.guildSettings")

                        while (res.next()) {
                            val guildId = toLong(res.getString("guildId"))

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
                            )
                        }
                        callback.invoke(settings)
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }

        }
    }

    override fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit) {
        throw MethodNotSupportedException("Not supported for SQLite")
    }

    override fun updateGuildSetting(settings: GuildSettings, callback: (Boolean) -> Unit) {
        val database = variables.database

        database.run {

            val dbName = database.name

            try {
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
                        "WHERE guildId='" + settings.guildId + "'")
                    smt.setBoolean(1, settings.isEnableJoinMessage)
                    smt.setBoolean(2, settings.isEnableSwearFilter)
                    smt.setString(3, fixUnicodeAndLines(settings.customJoinMessage))
                    smt.setString(4, replaceUnicode(settings.customPrefix))
                    smt.setString(5, settings.autoroleRole.toString())
                    smt.setString(6, settings.logChannel.toString())
                    smt.setString(7, settings.welcomeLeaveChannel.toString())
                    smt.setString(8, fixUnicodeAndLines(settings.customLeaveMessage))
                    smt.setString(9, fixUnicodeAndLines(settings.serverDesc))
                    smt.setBoolean(10, settings.isAnnounceTracks)
                    smt.setBoolean(11, settings.isAutoDeHoist)
                    smt.setBoolean(12, settings.isFilterInvites)
                    smt.setBoolean(13, settings.isEnableSpamFilter)
                    smt.setString(14, settings.muteRoleId.toString())
                    smt.setString(15, convertJ2S(settings.ratelimits))
                    smt.setBoolean(16, settings.kickState)
                    smt.executeUpdate()

                }
            } catch (e1: SQLException) {
                e1.printStackTrace()

                callback.invoke(false)
            }

            callback.invoke(true)
        }
    }

    override fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit) {
        val database = variables.database

        database.run {

            val dbName = database.name
            val guildId = guildSettings.guildId

            try {
                database.connManager.use { manager ->
                    val connection = manager.connection
                    val resultSet = connection.createStatement()

                        .executeQuery("SELECT id FROM $dbName.guildSettings WHERE guildId='$guildId'")
                    var rows = 0

                    while (resultSet.next()) {
                        rows++
                    }

                    if (rows == 0) {
                        val smt = connection.prepareStatement("INSERT INTO $dbName.guildSettings(guildId," +
                            "customWelcomeMessage, prefix, customLeaveMessage, ratelimits) " +
                            "VALUES('$guildId' , ? , ? , ? , ?)")

                        smt.setString(1, guildSettings.customJoinMessage)
                        smt.setString(2, Settings.PREFIX)
                        smt.setString(3, guildSettings.customLeaveMessage.replace("\\P{Print}".toRegex(), ""))
                        smt.setString(4, "20|45|60|120|240|2400".replace("\\P{Print}".toRegex(), ""))
                        smt.execute()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

                callback.invoke(false)
            }

            callback.invoke(true)
        }
    }

    private fun changeCommand(guildId: Long, invoke: String, message: String, isEdit: Boolean): Triple<Boolean, Boolean, Boolean>? {
        val database = variables.database

        val sqlQuerry = if (isEdit)
            "UPDATE customCommands SET message = ? WHERE guildId = ? AND invoke = ?"
        else
            "INSERT INTO customCommands(guildId, invoke, message) VALUES (? , ? , ?)"

        try {
            database.connManager.use { manager ->
                val conn = manager.connection

                val stm = conn.prepareStatement(sqlQuerry)

                stm.setString(if (isEdit) 2 else 1, guildId.toString())
                stm.setString(if (isEdit) 3 else 2, invoke)
                stm.setString(if (isEdit) 1 else 3, message)
                stm.execute()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            return Triple(false, false, false)
        }

        return null
    }
}
