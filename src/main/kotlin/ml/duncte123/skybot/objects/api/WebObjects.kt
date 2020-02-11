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
import ml.duncte123.skybot.utils.AirUtils
import java.util.*

data class KpopObject(val id: Int, val name: String, val band: String, val image: String)

//data class WarnObject(val userId: String, val warnings: List<Warning>)
data class Warning(val id: Int, val rawDate: String, /*val date: Date, val expiryDate: Date,*/ val modId: String, val reason: String, val guildId: String)

data class Ban
@JsonCreator constructor(@JsonProperty("id") val id: Int, @JsonProperty("modUserId") val modId: String,
                         @JsonProperty("userId") val userId: String, @JsonProperty("Username") val userName: String,
                         @JsonProperty("discriminator") val discriminator: String, @JsonProperty("guildId") val guildId: String)

data class Mute
@JsonCreator constructor(@JsonProperty("id") val id: Int, @JsonProperty("mod_id") val modId: String,
                         @JsonProperty("user_id") val userId: String, @JsonProperty("user_tag") val userTag: String,
                         @JsonProperty("guild_id") val guildId: String)

data class VcAutoRole(val guildId: Long, val voiceChannelId: Long, val roleId: Long)

data class Reminder(val id: Int, val user_id: Long, val reminder: String, val reminder_date: Date, val channel_id: Long) {
    @JsonCreator
    constructor(@JsonProperty("id") id: Int, @JsonProperty("user_id") user_id: Long,
                @JsonProperty("reminder") reminder: String,
                @JsonProperty("remind_create_date") reminder_date: String, @JsonProperty("channel_id") channel_id: Long) :
        this(id, user_id, reminder, AirUtils.fromDatabaseFormat(reminder_date), channel_id)
}


