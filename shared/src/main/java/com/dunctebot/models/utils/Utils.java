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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Utils {
    public static String replaceNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\\\n", "\n");
    }

    private static String fixNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\n", "\\\\n");
    }

    public static String replaceUnicode(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\P{Print}", "");
    }

    /*private static String replaceUnicodeAndLines(String s) {
        return replaceUnicode(replaceNewLines(s));
    }*/

    public static String fixUnicodeAndLines(String s) {
        return replaceUnicode(fixNewLines(replaceNewLines(s)));
    }

    public static String convertJ2S(long[] in) {
        return Arrays.stream(in).mapToObj(String::valueOf).collect(Collectors.joining("|", "", ""));
    }

    private static long[] convertS2J(String in) {
        if (in.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};
        return Arrays.stream(in.split("\\|")).mapToLong(Long::valueOf).toArray();
    }

    public static long[] ratelimmitChecks(String fromDb) {
        if (fromDb == null || fromDb.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};

        return convertS2J(fromDb.replaceAll("\\P{Print}", ""));
    }

    public static long toLong(@Nullable String s) {
        if (s == null) {
            return 0L;
        }

        try {
            return Long.parseUnsignedLong(s);
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    @Nonnull
    public static String colorToHex(int hex) {
        final int r = (hex & 0xFF0000) >> 16;
        final int g = (hex & 0xFF00) >> 8;
        final int b = (hex & 0xFF);

        return String.format("#%02x%02x%02x", r, g, b);
    }

    public static int colorToInt(String hex) {
        return Integer.parseInt(hex.substring(1), 16);
    }

    // TODO: DateUtils
    public static String makeDatePretty(TemporalAccessor accessor) {
        return TimeFormat.DATE_TIME_LONG.format(accessor);
    }

    public static OffsetDateTime fromDatabaseFormat(String date) {
        try {
            return OffsetDateTime.parse(date);
        }
        catch (DateTimeParseException e) {
            e.printStackTrace();

            return OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public static String getDatabaseDateFormat(ParsedDuration duration) {
        return getDatabaseDateFormat(getDatabaseDate(duration));
    }

    public static String getDatabaseDateFormat(OffsetDateTime date) {
        return date.truncatedTo(ChronoUnit.MILLIS).toString();
    }

    public static OffsetDateTime getDatabaseDate(ParsedDuration duration) {
        return OffsetDateTime.now(ZoneOffset.UTC).plus(duration.getMilis(), ChronoUnit.MILLIS);
    }
}
