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

package me.duncte123.skybot.utils;

import net.fellbaum.jemoji.Emoji;
import net.fellbaum.jemoji.EmojiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Adapted from https://gist.github.com/heyarny/71c246f2f7fa4d9d10904fb9d5b1fa1d
 */
public class TwemojiParser {
    private static final String BASE_URL = "https://raw.githubusercontent.com/twitter/twemoji/master/assets/72x72/";

    private TwemojiParser() {}

    /**
     * Parses a single emoji in the given text and returns its corresponding URL.
     * The URL is based on the Twitter Twemoji project and is in 72x72 resolution.
     *
     * @param text
     *     The text containing the emoji to be parsed.
     *
     * @return A URL representing the emoji in the given text.
     * Returns null if no emoji is found in the text.
     *
     * @see <a href="https://github.com/twitter/twemoji">Twitter Twemoji</a>
     */
    public static String parseOne(String text) {
        final List<Emoji> emojis = EmojiManager.extractEmojisInOrder(stripVariants(text));

        if (!emojis.isEmpty()) {
            final String iconId = grabTheRightIcon(emojis.getFirst());

            return BASE_URL + iconId + ".png";
        }

        return null;
    }

    /**
     * Parses all emojis in the given text and returns a list of their corresponding URLs.
     * The URLs are based on the Twitter Twemoji project and are in 72x72 resolution.
     *
     * @param text
     *     The text containing emojis to be parsed.
     *
     * @return A list of URLs representing the emojis in the given text.
     * Returns null if no emojis are found in the text.
     *
     * @see <a href="https://github.com/twitter/twemoji">Twitter Twemoji</a>
     */
    public static List<String> parseAll(String text) {
        final List<Emoji> emojis = EmojiManager.extractEmojisInOrder(stripVariants(text));

        if (emojis.isEmpty()) {
            return null;
        }

        final List<String> urls = new ArrayList<>();

        // Kinda copied from EmojiParser but it does not have the variants on it
        for (final Emoji emoji : emojis) {
            final String iconId = grabTheRightIcon(emoji);
            final String iconUrl = BASE_URL + iconId + ".png";

            urls.add(iconUrl);
        }

        return urls;
    }

    public static String stripVariants(String rawText) {
        // if variant is present as \uFE0F
        return rawText.indexOf('\u200D') < 0 ? rawText.replace("\uFE0F", "") : rawText;
    }

    private static String grabTheRightIcon(Emoji emoji) {
        return emoji.getEmoji().codePoints().mapToObj(
            operand -> Integer.toHexString(operand).toLowerCase(Locale.ROOT)
        ).collect(Collectors.joining("-"));
    }
}
