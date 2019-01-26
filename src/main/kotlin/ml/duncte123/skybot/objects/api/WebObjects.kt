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

@file:Author(nickname = "duncte123", author = "Duncan Sterken")

package ml.duncte123.skybot.objects.api

import ml.duncte123.skybot.Author
import java.sql.Date

data class LlamaObject(val id: Int, val file: String)

data class AlpacaObject(val file: String)

data class KpopObject(val id: Int, val name: String, val band: String, val image: String)

data class WarnObject(val userId: String, val warnings: List<Warning>)
data class Warning(val id: Int, val date: Date, val expiryDate: Date, val modId: String, val reason: String, val guildId: String)

data class Ban(val id: Int, val modId: String, val userId: String, val userName: String, val discriminator: String, val guildId: String)
data class Mute(val id: Int, val modId: String, val userId: String, val userTag: String, val guildId: String)

data class VcAutoRole(val guildId: Long, val voiceChannelId: Long, val roleId: Long)

