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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.natanbc.reliqua.request.PendingRequest;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.sentry.Sentry;
import me.duncte123.botcommons.StringUtils;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.requests.JSONRequestBody;
import me.duncte123.durationparser.Duration;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.entities.jda.FakeMember;
import ml.duncte123.skybot.objects.FakePendingRequest;
import ml.duncte123.skybot.objects.api.Reminder;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.JDAImpl;
import org.ocpsoft.prettytime.PrettyTime;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;
import static me.duncte123.botcommons.web.WebParserUtils.toJSONObject;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class AirUtils {

    public static boolean isURL(String url) {
        return url.matches("^https?:\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_|]");
    }

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

    public static String getUptime(long time) {
        return getUptime(time, false);
    }

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

    public static void stop(AudioUtils audioUtils, ShardManager manager) {
        stopMusic(audioUtils, manager);

        audioUtils.getPlayerManager().shutdown();
    }

    private static void stopMusic(AudioUtils audioUtils, ShardManager manager) {
        final TLongObjectMap<GuildMusicManager> temp = new TLongObjectHashMap<>(audioUtils.getMusicManagers());

        for (final long key : temp.keys()) {
            final Guild guild = manager.getGuildById(key);

            if (guild != null) {
                stopMusic(guild, audioUtils);
            }
        }
    }

    public static void stopMusic(Guild guild, AudioUtils audioUtils) {
        final GuildMusicManager mng = audioUtils.getMusicManagers().get(guild.getIdLong());

        if (mng == null) {
            return;
        }

        final LavalinkManager lavalinkManager = LavalinkManager.ins;

        mng.stopAndClear();

        if (lavalinkManager.isConnected(guild)) {
            lavalinkManager.closeConnection(guild);
        }
    }

    public static TextChannel getLogChannel(long channel, Guild g) {
        return getLogChannel(Long.toString(channel), g);
    }

    private static TextChannel getLogChannel(String channelId, Guild guild) {
        if (channelId == null || channelId.isEmpty()) return GuildUtils.getPublicChannel(guild);

        final List<TextChannel> foundChannels = FinderUtil.findTextChannels(channelId, guild);

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.get(0);
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
            final List<User> foundUsers = FinderUtils.searchUsers(ctx.getArgsRaw(), ctx);

            if (!foundUsers.isEmpty()) {
                target = foundUsers.get(0);
            }
        }

        return target;
    }

    public static Member getMentionedMember(String argument, CommandContext ctx) {
        final List<Member> foundMembers = FinderUtils.searchMembers(argument, ctx);

        if (foundMembers.isEmpty()) {
            return new FakeMember(argument);
        }

        return foundMembers.get(0);
    }

    private static DateTimeFormatter getIsoFormat() {
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));
//        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC+2"));
//        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
//        return DateTimeFormatter.ISO_INSTANT;
    }

    public static String getDatabaseDateFormat(Duration duration) {
        return getDatabaseDateFormat(getDatabaseDate(duration));
    }

    public static String getDatabaseDateFormat(Instant date) {
//        return getSimpleFormatter().format(Date.from((Instant) date));
//        return getIsoFormat().format(date);
        return date.truncatedTo(ChronoUnit.MILLIS).toString();
    }

    // Parsing goes wrong right here
    public static Instant fromDatabaseFormat(String date) {
        try {
//            return Date.from(Instant.parse(date));

            /*final Instant plus = getIsoFormat()
                .parse(date, Instant::from)
                .plus(2, ChronoUnit.HOURS);*/

//            System.out.println("input " + date);
//            System.out.println("plus " + plus);

//            final ZonedDateTime zonedDateTime = ZonedDateTime.parse(date).withZoneSameInstant(ZoneId.of("Europe/Paris"));
//            final ZonedDateTime zonedDateTime = ZonedDateTime.parse(date).withZoneSameInstant(ZoneId.systemDefault());
//
//            System.out.println(zonedDateTime);

//            return zonedDateTime;

//            return Date.from(getIsoFormat().parse(date, Instant::from));
//            return getIsoFormat().parse(date);
//            return getIsoFormat().parse(date,  Instant::from);
//            return Date.from(plus);
//            return plus;
//            return Instant.parse(date);
            return ZonedDateTime.parse(date)
                .toInstant()
                .plus(2, ChronoUnit.HOURS);
        }
        catch (DateTimeParseException e) {
            e.printStackTrace();

            return new Date().toInstant();
        }
    }

    private static SimpleDateFormat getSimpleFormatter() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format;
    }

    public static String makeDatePretty(Instant accessor) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC")).format(accessor);
//        return DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.systemDefault()).format(accessor);
//        return getSimpleFormatter().format(accessor);
    }

    public static Calendar toCalendar(Instant accessor) {
        final Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//        final Calendar utc = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
//        final Calendar utc = Calendar.getInstance();

        utc.setTimeInMillis(accessor.toEpochMilli());

        return utc;
    }

    public static Instant getEpochInstant() {
//        return Instant.ofEpochMilli(System.currentTimeMillis());
//        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).toInstant();
        return Instant.now();
//        return Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault())).toInstant();
    }

    public static Instant getDatabaseDate(Duration duration) {
        return Instant.now().plusMillis(duration.getMilis());
//        return Instant.ofEpochMilli(System.currentTimeMillis() + duration.getMilis());
//        return new Date(System.currentTimeMillis() + duration.getMilis());
    }

    public static void handleExpiredReminders(List<Reminder> reminders, DatabaseAdapter adapter, PrettyTime prettyTime) {
        // Get the shardManager and a list of ints to purge the ids for
        final ShardManager shardManager = SkyBot.getInstance().getShardManager();
        final List<Integer> toPurge = new ArrayList<>();

        for (final Reminder reminder : reminders) {
            // The reminder message template
            final String message = String.format(
                "%s you asked me to remind you about \"%s\"",
//                prettyTime.format(toCalendar(reminder.getCreate_date())),
//                prettyTime.format(toCalendar(reminder.getCreate_date())),
                prettyTime.format(Date.from(reminder.getCreate_date())),
                reminder.getReminder().trim()
            );

            final long channelId = reminder.getChannel_id();

            // If we have a channel send the message to that
            if (channelId > 0) {
                final TextChannel channel = shardManager.getTextChannelById(channelId);

                // If we don't have a channel we can't send it there
                // TODO: DM the user instead?
                if (channel != null) {
                    // Add the reminder to the list of the reminders to purge
                    toPurge.add(reminder.getId());
                    sendMsgFormat(channel, "<@%s>, %s", reminder.getUser_id(), message);
                }

                // go to the next one and don't run the user code
                continue;
            }

            try {
                Objects.requireNonNull(shardManager.getShardById(0))
                    .openPrivateChannelById(reminder.getUser_id())
                    .flatMap(
                        (c) -> c.sendMessage(message)
                    )
                    .complete();
                toPurge.add(reminder.getId());
            }
            catch (NullPointerException ignored) {
                // this should never happen, shard 0 is always there
            }
            catch (ErrorResponseException errorResponseEx) {
                final ErrorResponse errorResponse = errorResponseEx.getErrorResponse();

                if (
                    // The account probably got deleted or something
                    errorResponse == ErrorResponse.UNKNOWN_USER ||
                        // we cannot dm this user (has dms blocked?)
                        errorResponse == ErrorResponse.CANNOT_SEND_TO_USER
                ) {
                    toPurge.add(reminder.getId());
                }
            }
            catch (Exception e) {
                Sentry.capture(e);
            }
        }

        // Get a calendar instance that is two days in the future
        /*final Calendar calendarDateAfter = Calendar.getInstance();
        calendarDateAfter.add(Calendar.DAY_OF_YEAR, 2);

        final Date dateAfter = calendarDateAfter.getTime();*/

        final Instant plusTwoDays = Instant.now().plus(2L, ChronoUnit.DAYS);

        // Remove any reminders that have not been removed after 2 days
        final List<Integer> extraRemoval = reminders.stream()
            .filter((reminder) -> reminder.getReminder_date().isAfter(plusTwoDays))
//            .filter((reminder) -> reminder.getReminder_date().after(dateAfter))
            .map(Reminder::getId)
            .collect(Collectors.toList());

        toPurge.addAll(extraRemoval);
        adapter.purgeReminders(toPurge);
    }

    public static String parsePerms(Permission[] perms) {
        final String neededPerms = Arrays.stream(perms)
            .map(Permission::getName)
            .collect(Collectors.joining("`, `"));

        return StringUtils.replaceLast(neededPerms, "`, `", "` and `");
    }

    public static void setJDAContext(JDA jda) {
        ((JDAImpl) jda).setContext();
    }

    public static void loadGuildMembers(Guild guild) {
        try {
            guild.retrieveMembers().get();
        }
        catch (InterruptedException | ExecutionException e) {
            Sentry.capture(e);
        }
    }

    @Nonnull
    public static PendingRequest<String> shortenUrl(String url, String googleKey, ObjectMapper mapper) {
        final ObjectNode json = mapper.createObjectNode();

        json.set("dynamicLinkInfo",
            mapper.createObjectNode()
                .put("domainUriPrefix", "dunctebot.link")
                .put("link", url)
        );
        json.set("suffix",
            mapper.createObjectNode()
                .put("option", "SHORT") // SHORT or UNGUESSABLE
        );

        try {
            return WebUtils.ins.postRequest(
                "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + googleKey,
                JSONRequestBody.fromJackson(json)
            )
                .build(
                    (r) -> {
                        final ObjectNode response = toJSONObject(r, mapper);

                        if (response == null) {
                            return "Google did a fucky wucky and send invalid json";
                        }

                        return response.get("shortLink").asText();
                    },
                    WebParserUtils::handleError
                );
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();

            // Return a fake pending request to make sure that things don't break
            return new FakePendingRequest<>("JSON PARSING FAILED: " + e.getMessage());
        }
    }
}
