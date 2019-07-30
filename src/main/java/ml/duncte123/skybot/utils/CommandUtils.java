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

package ml.duncte123.skybot.utils;

import com.jagrosh.jagtag.JagTag;
import com.jagrosh.jagtag.Parser;
import gnu.trove.map.TLongLongMap;
import gnu.trove.set.TLongSet;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.jagtag.DiscordMethods;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;


/**
 * The methods {@link #splitInput(String)} and {@link #parseInput(Flag[], List)} have been rewritten in java from
 * JavaScript
 * The original methods are available at https://github.com/blargbot/blargbot/
 */
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CommandUtils {
    public static final TLongSet patrons = MapUtils.newLongSet();
    public static final TLongSet guildPatrons = MapUtils.newLongSet();
    // Key: user_id Value: guild_id
    public static final TLongLongMap oneGuildPatrons = MapUtils.newLongLongMap();
    public static final TLongSet tagPatrons = MapUtils.newLongSet();

    public static final Supplier<Parser> PARSER_SUPPLIER = () -> JagTag.newDefaultBuilder()
        .addMethods(DiscordMethods.getMethods())
        .build();

    /*public static List<String> splitInput(@Nonnull String content) {
        final List<String> input = new ArrayList<>(Arrays.asList(content.split("\\s+")));

        if (!input.isEmpty() && input.get(0).isBlank()) {
            input.remove(0);
        }
        if (!input.isEmpty() && input.get(input.size() - 1).isBlank()) {
            input.remove(input.size() - 1);
        }

        List<String> words = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder quoted = new StringBuilder();

        for (final String i : input) {
            if (!inQuote) {
                // Normal quote            Escaped quote
                if (i.startsWith("\"") && !i.startsWith("\\\"")) {
                    inQuote = true;
                    if (i.endsWith("\"") && !i.endsWith("\\\"")) {
                        inQuote = false;
                        words.add(i.substring(1, i.length() - 1));
                    } else {
                        quoted = new StringBuilder();
                        quoted.append(i.substring(1)).append(' ');
                    }
                } else {
                    words.add(i);
                }
            } else { // inQuote
                if (i.endsWith("\"") && !i.endsWith("\\\"")) {
                    inQuote = false;
                    //noinspection StringOperationCanBeSimplified
                    quoted.append(i.substring(0, i.length() - 1));
                    words.add(quoted.toString());
                } else {
                    quoted.append(i).append(' ');
                }
            }
        }

        if (inQuote) {
            words = input;
        }

        for (int i = 0; i < words.size(); i++) {
            words.set(i, words.get(i).replace("\"", ""));
        }

        return words;
    }*/

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
                        .filter((f) -> f.getWord() != null && f.getWord().equals(fWord.substring(2).toLowerCase()))
                        .collect(Collectors.toList());

                    if (!flags.isEmpty()) {
                        currentFlag = String.valueOf(flags.get(0).getFlag());
                        output.put(currentFlag, new ArrayList<>());
                        pushFlag = false;
                    }
                } else {
                    currentFlag = "";
                    pushFlag = false;
                }
            } else if (word.startsWith("-")) {
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
                if (!currentFlag.isBlank()) {
                    output.get(currentFlag).add(word);
                } else {
                    output.get("undefined").add(word);
                }
            }
        }

        return output;
    }

    public static String parseJagTag(CommandContext ctx, String content) {
        final Parser parser = PARSER_SUPPLIER.get()
            .put("messageId", ctx.getMessage().getId())
            .put("user", ctx.getAuthor())
            .put("channel", ctx.getChannel())
            .put("guild", ctx.getGuild())
            .put("args", ctx.getArgsJoined());
        final String parsed = parser.parse(content);

        parser.clear();

        return parsed;
    }

    public static boolean isPatron(@Nonnull User u, @Nullable TextChannel tc) {
        if (isDev(u) || patrons.contains(u.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(Settings.SUPPORT_GUILD_ID);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        if (m == null) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot).\n" +
                "You will also need to join our discord server [here](https://discord.gg/NKM9Xtk)"));
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(Settings.PATRONS_ROLE))) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot)."));
            return false;
        }

        patrons.add(u.getIdLong());

        return true;
    }

    public static boolean isPatron(@Nonnull User u, @Nullable TextChannel tc, boolean reply) {
        final TextChannel textChannel = reply ? tc : null;
        return isPatron(u, textChannel);
    }

    public static boolean isGuildPatron(@Nonnull User u, @Nonnull Guild g) {

        if (guildPatrons.contains(g.getIdLong()) || oneGuildPatrons.containsValue(g.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(Settings.SUPPORT_GUILD_ID);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        if (m == null) {
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(Settings.GUILD_PATRONS_ROLE))) {
            return false;
        }

        guildPatrons.add(g.getIdLong());

        return true;
    }

    public static boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent event, boolean reply) {
        final boolean isGuild = isGuildPatron(event.getAuthor(), event.getGuild());
        return isGuild || isPatron(event.getAuthor(), event.getChannel(), reply);
    }

    public static boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent e) {
        return isUserOrGuildPatron(e, true);
    }

    public static boolean isDev(@Nonnull User u) {
        return Settings.DEVELOPERS.contains(u.getIdLong());
    }
}
