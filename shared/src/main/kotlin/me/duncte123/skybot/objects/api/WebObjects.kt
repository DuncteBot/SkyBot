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

package me.duncte123.skybot.objects.api

import com.dunctebot.models.utils.DateUtils
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.ZonedDateTime

// data class WarnObject(val userId: String, val warnings: List<Warning>)
// TODO: insert user id here
data class Warning(
    val id: Int,
    val rawDate: String, /*val date: Date, val expiryDate: Date,*/
    val modId: Long,
    val reason: String,
    val guildId: Long
)

data class Ban(
    val id: Int,
    val modId: Long,
    val userId: Long,
    val guildId: Long
)

data class BanBypas(
    val guildId: Long,
    val userId: Long
)

data class Mute(
    val id: Int,
    val modId: Long,
    val userId: Long,
    val guildId: Long
)

data class VcAutoRole(val guildId: Long, val voiceChannelId: Long, val roleId: Long)

data class Reminder(
    val id: Int,
    val user_id: Long,
    val reminder: String,
    val create_date: ZonedDateTime,
    val reminder_date: ZonedDateTime,
    val channel_id: Long,
    val message_id: Long,
    val guild_id: Long,
    val in_channel: Boolean
) {
    val reminderDateDate: String = DateUtils.makeDatePretty(reminder_date)
    val reminderCreateDateDate: String = DateUtils.makeDatePretty(create_date)

    val jumpUrl = "https://discord.com/channels/$guild_id/$channel_id/$message_id"

    override fun toString(): String {
        // TODO: this is an hour behind
        return "$id) `$reminder` on ${TimeFormat.DATE_TIME_LONG.format(reminder_date)}"
    }
}

data class Patron(
    val type: Type,
    val userId: Long,
    val guildId: Long?
) {
    enum class Type {
        NORMAL,
        TAG,
        ONE_GUILD,
        ALL_GUILD;
    }
}

data class AllPatronsData(
    val patrons: List<Patron>,
    val tagPatrons: List<Patron>,
    val oneGuildPatrons: List<Patron>,
    val guildPatrons: List<Patron>
) {
    companion object {
        @JvmStatic
        fun fromSinglePatron(patron: Patron): AllPatronsData {
            return when (patron.type) {
                Patron.Type.NORMAL -> AllPatronsData(
                    listOf(patron),
                    listOf(),
                    listOf(),
                    listOf()
                )
                Patron.Type.TAG -> AllPatronsData(
                    listOf(),
                    listOf(patron),
                    listOf(),
                    listOf()
                )
                Patron.Type.ONE_GUILD -> AllPatronsData(
                    listOf(),
                    listOf(),
                    listOf(patron),
                    listOf()
                )
                Patron.Type.ALL_GUILD -> AllPatronsData(
                    listOf(),
                    listOf(),
                    listOf(),
                    listOf(patron)
                )
            }
        }
    }
}
