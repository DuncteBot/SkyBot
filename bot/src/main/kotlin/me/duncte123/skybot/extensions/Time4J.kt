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

package me.duncte123.skybot.extensions

import com.dunctebot.models.utils.DateUtils
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.utils.TimeFormat
import java.time.OffsetDateTime

fun ISnowflake.parseTimeCreated(): Pair<String, String> {
    return this.timeCreated.parseTimes()
}

fun Member.parseTimeJoined(): Pair<String, String> {
    return this.timeJoined.parseTimes()
}

fun OffsetDateTime.parseTimes(timeFormat: TimeFormat = TimeFormat.RELATIVE): Pair<String, String> {
    val createTimeFormat = DateUtils.makeDatePretty(this)
    val createTimeHuman: String = this.humanize(timeFormat)

    return createTimeFormat to createTimeHuman
}

// old method, in case we ever need that

/*fun OffsetDateTime.humanize(textWidth: TextWidth = TextWidth.WIDE): String {
    val start = PlainTimestamp.from(this.toLocalDateTime())
    val end = PlainTimestamp.from(LocalDateTime.now(ZoneOffset.UTC))
    // duration is between the offset date time we get and now
    val duration = Duration.`in`(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS).between(start, end)

    // PrettyTime.of uses an internal cache so we don't need to worry about that
    return PrettyTime.of(Locale.ENGLISH).print(duration, textWidth) + " ago"
}*/

fun OffsetDateTime.humanize(timeFormat: TimeFormat = TimeFormat.RELATIVE): String {
    return timeFormat.format(this)
}
