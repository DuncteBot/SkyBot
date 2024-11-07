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

import com.jagrosh.jagtag.JagTag;
import com.jagrosh.jagtag.Parser;
import me.duncte123.skybot.Settings;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import me.duncte123.skybot.objects.jagtag.DiscordMethods;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The {@link #parseInput(Flag[], List)} method has been rewritten in java from
 * JavaScript
 * The original method is available at <a href="https://github.com/blargbot/blargbot/">https://github.com/blargbot/blargbot/</a>
 */
public class CommandUtils {
    public static final Supplier<Parser> PARSER_SUPPLIER = () -> JagTag.newDefaultBuilder()
        .addMethods(DiscordMethods.getMethods())
        .build();

    private CommandUtils() {
    }

    @Nonnull
    public static Map<String, List<String>> parseInput(Flag[] map, @Nonnull List<String> words) {
        final Map<String, List<String>> output = new ConcurrentHashMap<>();
        output.put("undefined", new ArrayList<>());
        String currentFlag = "";

        for (final String s : words) {
            boolean pushFlag = true;
            String word = s;

            if (word.startsWith("--")) {
                if (word.length() > 2) {
                    final String fWord = word;
                    final List<Flag> flags = Arrays.stream(map)
                        .filter((f) -> f.getWord() != null && f.getWord().equalsIgnoreCase(fWord.substring(2)))
                        .toList();

                    if (flags.isEmpty()) {
                        currentFlag = word.substring(2);
                    } else {
                        final Flag flag = flags.get(0);
                        if (flag.getFlag() == null && flag.getWord() != null) {
                            currentFlag = flag.getWord();
                        } else {
                            currentFlag = String.valueOf(flag.getFlag());
                        }
                    }

                    output.put(currentFlag, new ArrayList<>());
                } else {
                    currentFlag = "";
                }

                pushFlag = false;
            } else if (word.charAt(0) == '-') {
                if (word.length() > 1) {
                    final String tempFlag = word.substring(1);

                    for (int i1 = 0; i1 < tempFlag.length(); i1++) {
                        currentFlag = String.valueOf(tempFlag.charAt(i1));
                        output.put(currentFlag, new ArrayList<>());
                    }
                    pushFlag = false;
                }
            } else if (word.startsWith("\\-")) {
                word = word.substring(1);
            }

            if (pushFlag) {
                if (currentFlag.isBlank()) {
                    output.get("undefined").add(word);
                } else {
                    output.get(currentFlag).add(word);
                }
            }
        }

        return output;
    }

    public static Parser getParser(CommandContext ctx) {
        return PARSER_SUPPLIER.get()
            .put("messageId", ctx.getMessage().getId())
            .put("user", ctx.getAuthor())
            .put("channel", ctx.getChannel())
            .put("guild", ctx.getGuild())
            .put("args", ctx.getArgsJoined());
    }

    public static String parseJagTag(CommandContext ctx, String content) {
        final Parser parser = getParser(ctx);
        final String parsed = parser.parse(content);

        parser.clear();

        return parsed;
    }

    public static boolean isDev(@Nonnull User user) {
        return isDev(user.getIdLong());
    }

    public static boolean isDev(long userId) {
        for (final long id : Settings.DEVELOPERS) {
            if (id == userId) {
                return true;
            }
        }

        return false;
    }
}
