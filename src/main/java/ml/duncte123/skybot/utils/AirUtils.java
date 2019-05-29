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

import com.github.natanbc.reliqua.request.PendingRequest;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.entities.jda.FakeMember;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class AirUtils {
    private static final Pattern UNIX_UPTIME_PATTERN = Pattern.compile("(?:.*)up(.*)[0-9] users(?:.*)");

    /**
     * This will validate a link
     *
     * @param url
     *         The thing to check
     *
     * @return true or false depending on if the url is valid
     */
    public static boolean isURL(String url) {
        return Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)").matcher(url).find();
    }

    /**
     * This will check if the number that we are trying to parse is an int
     *
     * @param integer
     *         the int to check
     *
     * @return true if it is an int
     */
    public static boolean isInt(String integer) {
        return integer.matches("^\\d{1,11}$");
    }


    public static int parseIntSafe(String integer) {
        if (isInt(integer)) {
            return Integer.parseInt(integer);
        }

        return -1;
    }

    private static int longToInt(long input) {
        return (int) input;
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time
     *         The time that the bot has been running for
     *
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time) {
        return getUptime(time, false);
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time
     *         The time that the bot has been running for
     * @param withTime
     *         If we should add the seconds, minutes and hours to the time
     *
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time, boolean withTime) {
        /*
        This code has been inspired from JDA-Butler <https://github.com/Almighty-Alpaca/JDA-Butler/>
         */
        //Like it's ever gonna be up for more then a week
        final int years = longToInt(time / 31104000000L);
        final int months = longToInt(time / 2592000000L % 12);
        final int days = longToInt(time / 86400000L % 30);

        final StringBuilder builder = new StringBuilder();

        //Get the years, months and days
        builder.append(formatTimeWord("Year", years, true));
        builder.append(formatTimeWord("Month", months, true));
        builder.append(formatTimeWord("Day", days, false));

        //If we want the time added we pass in true
        if (withTime) {
            final int hours = longToInt(time / 3600000L % 24);
            final int minutes = longToInt(time / 60000L % 60);
            final int seconds = longToInt(time / 1000L % 60);

            builder.append(", ");
            builder.append(formatTimeWord("Hour", hours, true));
            builder.append(formatTimeWord("Minute", minutes, true));
            builder.append(formatTimeWord("Second", seconds, false));
        }

        final String uptimeString = builder.toString();

        return uptimeString.startsWith(", ") ? uptimeString.replaceFirst(", ", "") : uptimeString;
    }

    private static String formatTimeWord(String word, int amount, boolean withComma) {
        if (amount == 0) {
            return "";
        }

        final StringBuilder builder = new StringBuilder()
            .append(amount).append(' ').append(word);

        if (amount > 1) {
            builder.append('s');
        }

        if (withComma) {
            builder.append(", ");
        }

        return builder.toString();
    }

    /**
     * Stops everything
     */
    public static void stop(DBManager database, AudioUtils audioUtils) {
        final TLongObjectMap<GuildMusicManager> temp = new TLongObjectHashMap<>(audioUtils.musicManagers);

        for (final long key : temp.keys()) {

            final GuildMusicManager mng = audioUtils.musicManagers.get(key);

            if (mng.player.getPlayingTrack() != null) {
                mng.player.stopTrack();
            }
        }

        database.getService().shutdown();
    }

    public static TextChannel getLogChannel(long channel, Guild g) {
        return getLogChannel(Long.toString(channel), g);
    }

    /**
     * This gets the channel from a name or id
     *
     * @param channelId
     *         the channel name or id
     * @param guild
     *         the guild to search in
     *
     * @return the channel
     */
    public static TextChannel getLogChannel(String channelId, Guild guild) {
        if (channelId == null || channelId.isEmpty()) return GuildUtils.getPublicChannel(guild);

        final List<TextChannel> foundChannels = FinderUtil.findTextChannels(channelId, guild);

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.get(0);
    }

    public static PendingRequest<String> shortenUrl(String url, String googleKey) {
        return WebUtils.ins.shortenUrl(url, "lnk.dunctebot.com", googleKey);
    }

    public static String colorToHex(int hex) {
        return colorToHex(new Color(hex));
    }

    public static String colorToHex(@Nullable Color color) {
        if (color == null) {
            return "#1FFFFFF";
        }

        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Returns the system uptime
     *
     * @return String lala
     *
     * @throws Exception
     */
    public static String getSystemUptime() throws Exception {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
//            Process uptimeProc = Runtime.getRuntime().exec("uptime | awk -F'( |,|:)+' '{print $6,$7\",\",$8,\"hours,\",$9,\"minutes\"}'");
            final Process uptimeProc = Runtime.getRuntime().exec("uptime");
            final BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
            final String line = in.readLine();
            if (line != null) {
                final Matcher matcher = UNIX_UPTIME_PATTERN.matcher(line);
                if (matcher.find()) {
                    return matcher.group(matcher.groupCount());
                }
            }
        }
        return "";
    }

    public static User getMentionedUser(CommandContext ctx) {
        User target = ctx.getAuthor();

        if (!ctx.getArgs().isEmpty()) {
            final List<User> foundUsers = FinderUtil.findUsers(ctx.getArgsRaw(), ctx.getJDA());

            if (!foundUsers.isEmpty()) {
                target = foundUsers.get(0);
            }
        }

        return target;
    }

    public static Member getMentionedMember(String argument, Guild guild) {
        final List<Member> foundMembers = FinderUtil.findMembers(argument, guild);

        if (foundMembers.isEmpty()) {
            return new FakeMember(argument);
        }

        return foundMembers.get(0);
    }
}
