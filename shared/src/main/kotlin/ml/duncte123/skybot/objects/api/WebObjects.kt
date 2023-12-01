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

package ml.duncte123.skybot.objects.api

import com.dunctebot.models.utils.DateUtils
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.ZonedDateTime

data class KpopObject(val id: Int, val name: String, val band: String, val image: String)

// data class WarnObject(val userId: String, val warnings: List<Warning>)
// TODO: insert user id here
data class Warning(
    val id: Int,
    val rawDate: String, /*val date: Date, val expiryDate: Date,*/
    val modId: Long,
    val reason: String,
    val guildId: Long
)

// TODO: make sure id props are longs
data class Ban
@JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("modUserId") val modId: String,
    @JsonProperty("userId") val userId: Long,
    @Deprecated("Useless") @JsonProperty("Username") val userName: String,
    @Deprecated("Useless") @JsonProperty("discriminator") val discriminator: String,
    @JsonProperty("guildId") val guildId: String
)

data class BanBypas
@JsonCreator constructor(
    @JsonProperty("guild_id") val guildId: Long,
    @JsonProperty("user_id") val userId: Long
)

data class Mute
@JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("mod_id") val modId: Long,
    @JsonProperty("user_id") val userId: Long,
    @Deprecated("Useless") @JsonProperty("user_tag") val userTag: String,
    @JsonProperty("guild_id") val guildId: Long
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
    @JsonCreator
    constructor(
        @JsonProperty("id") id: Int,
        @JsonProperty("user_id") user_id: Long,
        @JsonProperty("reminder") reminder: String,
        @JsonProperty("remind_create_date") create_date: String,
        @JsonProperty("remind_date") reminder_date: String,
        @JsonProperty("channel_id") channel_id: Long,
        @JsonProperty("message_id") message_id: Long,
        @JsonProperty("guild_id") guild_id: Long,
        @JsonProperty("in_channel") in_channel: Boolean
    ) :
        this(
            id, user_id, reminder, DateUtils.fromDatabaseFormat(create_date),
            DateUtils.fromDatabaseFormat(reminder_date), channel_id, message_id, guild_id, in_channel
        )

    val reminderDateDate: String = DateUtils.makeDatePretty(reminder_date)
    val reminderCreateDateDate: String = DateUtils.makeDatePretty(create_date)

    val jumpUrl = "https://discord.com/channels/$guild_id/$channel_id/$message_id"

    override fun toString(): String {
        return "$id) `$reminder` on ${TimeFormat.DATE_TIME_LONG.format(reminder_date)}"
    }
}

data class Patron
@JsonCreator constructor(
    @JsonProperty("type") val type: Type,
    @JsonProperty("user_id") val userId: Long,
    @JsonProperty("guild_id") val guildId: Long?
) {
    enum class Type {
        NORMAL,
        TAG,
        ONE_GUILD,
        ALL_GUILD;
    }
}

data class AllPatronsData @JsonCreator constructor(
    @JsonProperty("patrons") val patrons: List<Patron>,
    @JsonProperty("tag_patrons") val tagPatrons: List<Patron>,
    @JsonProperty("one_guild_patrons") val oneGuildPatrons: List<Patron>,
    @JsonProperty("guild_patrons") val guildPatrons: List<Patron>
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
