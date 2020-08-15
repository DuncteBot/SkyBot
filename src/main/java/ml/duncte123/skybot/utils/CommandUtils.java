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

package ml.duncte123.skybot.utils;

import com.jagrosh.jagtag.JagTag;
import com.jagrosh.jagtag.Parser;
import gnu.trove.map.TLongLongMap;
import gnu.trove.set.TLongSet;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.api.AllPatronsData;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.jagtag.DiscordMethods;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

/**
 * The {@link #parseInput(Flag[], List)} method has been rewritten in java from
 * JavaScript
 * The original method is available at https://github.com/blargbot/blargbot/
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
                    pushFlag = false;
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

    private static boolean isPatron(@Nonnull User u, @Nullable TextChannel tc) {
        // Developers have access to paton features
        if (isDev(u) || patrons.contains(u.getIdLong())) {
            return true;
        }

        //noinspection ConstantConditions
        final Guild supportGuild = u.getJDA().getShardManager().getGuildById(Settings.SUPPORT_GUILD_ID);

        // If the guild is not in cache (cuz discord) ignore the rest of the checks
        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        // If the member is not in our guild we tell them to join it
        if (m == null) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot).\n" +
                "You will also need to join our discord server [here](https://dunctebot.link/server)"), false);
            return false;
        }

        // If the member is not a patron tell them to become one
        if (!m.getRoles().contains(supportGuild.getRoleById(Settings.PATRONS_ROLE))) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot)."), false);
            return false;
        }

        patrons.add(u.getIdLong());

        return true;
    }

    public static boolean isUserTagPatron(@Nonnull User u) {
        return tagPatrons.contains(u.getIdLong()) || isDev(u);
    }

    private static boolean isPatron(@Nonnull User u, @Nullable TextChannel tc, boolean reply) {
        final TextChannel textChannel = reply ? tc : null;
        return isPatron(u, textChannel) || isUserTagPatron(u);
    }

    public static boolean isGuildPatron(@Nonnull Guild g) {
        return oneGuildPatrons.containsValue(g.getIdLong()) || shouldGuildBeConsideredPremium(g);
    }

    // FIXME: Do new patron checks for guilds
    private static boolean isGuildPatron(@Nonnull User u, @Nonnull Guild g) {
        // Check if the guild is a patron either via user-being admin or as a one-guild patron
        if (oneGuildPatrons.containsValue(g.getIdLong()) || shouldGuildBeConsideredPremium(g)) {
            return true;
        }

        //noinspection ConstantConditions
        final Guild supportGuild = u.getJDA().getShardManager().getGuildById(Settings.SUPPORT_GUILD_ID);

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

        // We're adding the user here to make the checks easier
        guildPatrons.add(u.getIdLong());

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
        return isDev(u.getIdLong());
    }

    public static boolean isDev(long userId) {
        for (final long id : Settings.DEVELOPERS) {
            if (id == userId) {
                return true;
            }
        }

        return false;
    }

    private static boolean shouldGuildBeConsideredPremium(@Nonnull Guild g) {
        final AtomicBoolean foundPatron = new AtomicBoolean(false);

        guildPatrons.forEach((userId) -> {
            // Check if we have the member in the guild and if they are an admin
            final Member m = g.getMemberById(userId);
            final boolean userInGuild = m != null && m.hasPermission(Permission.ADMINISTRATOR);

            // Only set if we found a patron
            if (userInGuild) {
                foundPatron.set(true);
            }

            // return false to stop looping
            return !userInGuild;
        });

        return foundPatron.get();
    }

    public static void addPatronsFromData(@Nonnull AllPatronsData data) {
        Checks.notNull(data, "data");

        data.getPatrons().forEach(
            (patron) -> patrons.add(patron.getUserId())
        );

        data.getTagPatrons().forEach(
            (patron) -> tagPatrons.add(patron.getUserId())
        );

        data.getOneGuildPatrons().forEach(
            (patron) -> oneGuildPatrons.put(patron.getUserId(), patron.getGuildId())
        );

        data.getGuildPatrons().forEach(
            (patron) -> guildPatrons.add(patron.getUserId())
        );
    }

    public static void removePatronsFromData(@Nonnull AllPatronsData data) {
        Checks.notNull(data, "data");

        data.getPatrons().forEach(
            (patron) -> patrons.remove(patron.getUserId())
        );

        data.getTagPatrons().forEach(
            (patron) -> tagPatrons.remove(patron.getUserId())
        );

        data.getOneGuildPatrons().forEach(
            (patron) -> oneGuildPatrons.remove(patron.getUserId())
        );

        data.getGuildPatrons().forEach(
            (patron) -> guildPatrons.remove(patron.getUserId())
        );
    }
}
