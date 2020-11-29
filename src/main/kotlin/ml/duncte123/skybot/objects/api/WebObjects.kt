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

@file:Author(nickname = "duncte123", author = "Duncan Sterken")

package ml.duncte123.skybot.objects.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.utils.AirUtils
import java.time.OffsetDateTime

data class KpopObject(val id: Int, val name: String, val band: String, val image: String)

// data class WarnObject(val userId: String, val warnings: List<Warning>)
data class Warning(
    val id: Int,
    val rawDate: String, /*val date: Date, val expiryDate: Date,*/
    val modId: String,
    val reason: String,
    val guildId: String
)

data class Ban
@JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("modUserId") val modId: String,
    @JsonProperty("userId") val userId: String,
    @JsonProperty("Username") val userName: String,
    @JsonProperty("discriminator") val discriminator: String,
    @JsonProperty("guildId") val guildId: String
)

data class Mute
@JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("mod_id") val modId: String,
    @JsonProperty("user_id") val userId: String,
    @JsonProperty("user_tag") val userTag: String,
    @JsonProperty("guild_id") val guildId: String
)

data class VcAutoRole(val guildId: Long, val voiceChannelId: Long, val roleId: Long)

data class Reminder(
    val id: Int,
    val user_id: Long,
    val reminder: String,
    val create_date: OffsetDateTime,
    val reminder_date: OffsetDateTime,
    val channel_id: Long,
    val message_id: Long,
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
        @JsonProperty("in_channel") in_channel: Boolean
    ) :
        this(
            id, user_id, reminder, AirUtils.fromDatabaseFormat(create_date),
            AirUtils.fromDatabaseFormat(reminder_date), channel_id, message_id, in_channel
        )

    val reminderDateDate: String = AirUtils.makeDatePretty(reminder_date)
    val reminderCreateDateDate: String = AirUtils.makeDatePretty(create_date)

    val jumpUrl: String
        get() {
            // HACK: very hacky
            val guildId = SkyBot.getInstance().shardManager
                .getTextChannelById(channel_id)?.guild?.idLong ?: "(failed to create jump url: missing guild info)"

            return "https://discord.com/channels/$guildId/$channel_id/$message_id"
        }

    override fun toString(): String {
        return "$id) `$reminder` on $reminderDateDate"
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
