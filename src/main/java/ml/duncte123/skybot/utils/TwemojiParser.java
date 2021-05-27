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

package ml.duncte123.skybot.utils;

import com.vdurmont.emoji.EmojiParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from https://gist.github.com/heyarny/71c246f2f7fa4d9d10904fb9d5b1fa1d
 */
public class TwemojiParser extends EmojiParser {
    private static final String BASE_URL = "https://twemoji.maxcdn.com/v/latest/72x72/";

    public static String parseOne(String text) {
        final List<UnicodeCandidate> emojis = getUnicodeCandidates(stripVariants(text));

        if  (!emojis.isEmpty()) {
            final String iconId = grabTheRightIcon(emojis.get(0).getEmoji().getUnicode());

            return BASE_URL + iconId + ".png";
        }

        return null;
    }

    // for future use
    public static List<String> parseAll(String text) {
        final List<UnicodeCandidate> emojis = getUnicodeCandidates(stripVariants(text));

        if (emojis.isEmpty()) {
            return null;
        }

        final List<String> urls = new ArrayList<>();

        // Kinda copied from EmojiParser but it does not have the variants on it
        for (final UnicodeCandidate emoji : emojis) {
            final String iconId = grabTheRightIcon(emoji.getEmoji().getUnicode());
            final String iconUrl = BASE_URL + iconId + ".png";

            urls.add(iconUrl);
        }

        return urls;
    }

    private static String toCodePoint(String unicodeSurrogates) {
        final List<String> codes = new ArrayList<>();

        int charAt;
        int someValue = 0; // what is for?
        int index = 0;

        while (index < unicodeSurrogates.length()) {
            charAt = unicodeSurrogates.charAt(index++);

            if (someValue == 0) {
                if (0xD800 <= charAt && charAt <= 0xDBFF) {
                    someValue = charAt;
                } else {
                    codes.add(Integer.toString(charAt, 16));
                }
            } else {
                final int calculation = 0x10000 + ((someValue - 0xD800) << 10) + (charAt - 0xDC00);

                codes.add(Integer.toString(calculation, 16));
                someValue = 0;
            }
        }

        return String.join("-", codes);
    }

    private static String stripVariants(String rawText) {
        // if variant is present as \uFE0F
        return rawText.indexOf('\u200D') < 0 ? rawText.replace("\uFE0F", "") : rawText;
    }

    private static String grabTheRightIcon(String rawText) {
        return toCodePoint(stripVariants(rawText));
    }
}
