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
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.durationparser.Duration;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.entities.jda.FakeMember;
import ml.duncte123.skybot.objects.api.Reminder;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class AirUtils {

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
    public static void stop(DBManager database, AudioUtils audioUtils, ShardManager manager) {
        final TLongObjectMap<GuildMusicManager> temp = new TLongObjectHashMap<>(audioUtils.musicManagers);

        for (final long key : temp.keys()) {
            final GuildMusicManager mng = audioUtils.musicManagers.get(key);
            final Guild guild = manager.getGuildById(key);
            final LavalinkManager lavalinkManager = LavalinkManager.INSTANCE;

            if (mng.player.getPlayingTrack() != null) {
                mng.player.stopTrack();
            }

            if (guild != null && lavalinkManager.isConnected(guild)) {
                lavalinkManager.closeConnection(guild);
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
    private static TextChannel getLogChannel(String channelId, Guild guild) {
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
        final int r = (hex & 0xFF0000) >> 16;
        final int g = (hex & 0xFF00) >> 8;
        final int b = (hex & 0xFF);

        return String.format("#%02x%02x%02x", r, g, b);
    }

    public static int colorToInt(String hex) {
        final String hexValue = "0x" + hex.replaceFirst("#", "");

        return Integer.decode(hexValue);
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

    private static SimpleDateFormat getFormatter() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        return format;
    }

    public static String getDatabaseDateFormat(Duration duration) {
        return getFormatter().format(getDatabaseDate(duration));
    }

    public static String getDatabaseDateFormat(Date date) {
        return getFormatter().format(date);
    }

    public static Date fromDatabaseFormat(String date) {
        try {
            return getFormatter().parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();

            return new Date();
        }
    }

    public static Date getDatabaseDate(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.getMilis());
    }

    public static void handleExpiredReminders(List<Reminder> reminders, DatabaseAdapter adapter, PrettyTime prettyTime) {
        final ShardManager shardManager = SkyBot.getInstance().getShardManager();
        final List<Integer> toPurge = new ArrayList<>();

        for (final Reminder reminder : reminders) {
            final String message = String.format(
                "%s you asked me to remind you about %s",
                prettyTime.format(reminder.getReminder_date()),
                reminder.getReminder().trim()
            );

            final long channelId = reminder.getChannel_id();

            if (channelId > 0) {
                final TextChannel channel = shardManager.getTextChannelById(channelId);

                if (channel != null) {
                    toPurge.add(reminder.getId());
                    sendMsgFormat(channel, "<@%s>, %s", reminder.getUser_id(), message);
                }
            } else {
                final User user = shardManager.getUserById(reminder.getUser_id());

                if (user != null) {
                    toPurge.add(reminder.getId());
                    user.openPrivateChannel().queue(
                        (channel) -> channel.sendMessage(message).queue()
                    );
                }
            }
        }

        // Remove any reminders that have not been removed after 2 days
        final Calendar calendarDateAfter = Calendar.getInstance();
        calendarDateAfter.add(Calendar.DAY_OF_YEAR, 2);

        final Date dateAfter = calendarDateAfter.getTime();

        final List<Integer> extraRemoval = reminders.stream()
            .filter((reminder) -> reminder.getReminder_date().after(dateAfter))
            .map(Reminder::getId)
            .collect(Collectors.toList());

        toPurge.addAll(extraRemoval);
        adapter.purgeReminders(toPurge);
    }
}
