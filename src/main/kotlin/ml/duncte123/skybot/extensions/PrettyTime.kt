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

package ml.duncte123.skybot.extensions

import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Member
import org.ocpsoft.prettytime.PrettyTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun PrettyTime.parseTimes(entity: ISnowflake): Pair<String, String> {
    return this.parseTimes(entity.timeCreated)
}

fun PrettyTime.parseTimes(entity: Member): Pair<String, String> {
    return this.parseTimes(entity.timeJoined)
}

fun PrettyTime.parseTimes(time: OffsetDateTime): Pair<String, String> {
    val createTimeDate = Date.from(time.toInstant())
    val createTimeFormat = time.format(DateTimeFormatter.RFC_1123_DATE_TIME)
    val createTimeHuman: String = this.format(createTimeDate)

    return createTimeFormat to createTimeHuman
}
