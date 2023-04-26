/*
 * MIT License
 *
 * Copyright (c) 2020 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.dunctebot.models.utils;

import me.duncte123.durationparser.ParsedDuration;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;

public class DateUtils {
    public static final ZoneId DB_ZONE_ID = ZoneId.of("Europe/London");

    public static String makeDatePretty(TemporalAccessor accessor) {
        return TimeFormat.DATE_TIME_LONG.format(accessor);
    }

    public static OffsetDateTime fromMysqlFormat(String date) {
        try {
            System.out.println(date.replace(" ", "T") + DB_ZONE_ID.getId());
            return OffsetDateTime.parse(date.replace(" ", "T") + DB_ZONE_ID.getId());
        }
        catch (DateTimeParseException e) {
            e.printStackTrace();

            return OffsetDateTime.now(DB_ZONE_ID);
        }
    }

    // TODO: use UTC in the future
    public static Timestamp getSqlTimestamp(OffsetDateTime date) {
        final var zonedDate = date.toZonedDateTime().withZoneSameInstant(DB_ZONE_ID);
        final var shouldWork = zonedDate.toString()
            .replace("Z", "")
            .replace("T", " ")
            .split("\\+")[0];

        return Timestamp.valueOf(shouldWork);
    }

    public static OffsetDateTime fromDatabaseFormat(String date) {
        try {
            return OffsetDateTime.parse(date);
        }
        catch (DateTimeParseException e) {
            e.printStackTrace();

            return OffsetDateTime.now(DB_ZONE_ID);
        }
    }

    public static String getDatabaseDateFormat(ParsedDuration duration) {
        return getDatabaseDateFormat(getDatabaseDate(duration));
    }

    public static String getDatabaseDateFormat(OffsetDateTime date) {
        return date.truncatedTo(ChronoUnit.MILLIS).toString();
    }

    public static OffsetDateTime getDatabaseDate(ParsedDuration duration) {
        return OffsetDateTime.now(DB_ZONE_ID).plus(duration.getMilis(), ChronoUnit.MILLIS);
    }
}
