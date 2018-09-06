/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import com.wolfram.alpha.WAEngine;
import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.discord.user.Profile;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"ReturnInsideFinallyBlock", "WeakerAccess", "unused"})
public class AirUtils {
    private static final Logger logger = LoggerFactory.getLogger(AirUtils.class);
    private static final Pattern UNIX_UPTIME_PATTERN = Pattern.compile("(?:.*)up(.*)[0-9] users(?:.*)");

    /**
     * This converts the online status of a user to a fancy emote
     *
     * @param status The {@link OnlineStatus} to convert
     * @return The fancy converted emote as a mention
     */
    public static String convertStatus(OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return "<:online2:464520569975603200>";
            case IDLE:
                return "<:away2:464520569862357002>";
            case DO_NOT_DISTURB:
                return "<:dnd2:464520569560498197>";

            default:
                return "<:offline2:464520569929334784>";
        }
    }

    /**
     * This will validate a link
     *
     * @param url The thing to check
     * @return true or false depending on if the url is valid
     */
    public static boolean isURL(String url) {
        return Pattern.compile("[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)").matcher(url).find();
    }

    /**
     * This will check if the number that we are trying to parse is an int
     *
     * @param integer the int to check
     * @return true if it is an int
     */
    public static boolean isInt(String integer) {
        return integer.matches("^\\d{1,11}$");
    }

    /**
     * This converts the game that a user is playing into a nice and readable format
     *
     * @param g the {@link net.dv8tion.jda.core.entities.Game Game} that the {@link net.dv8tion.jda.core.entities.Member Member} has
     * @return a nicely formatted game string
     */
    public static String gameToString(Game g) {
        if (g == null) return "no game";

        String gameType;

        switch (g.getType().getKey()) {
            case 1:
                gameType = "Streaming";
                break;
            case 2:
                gameType = "Listening to";
                break;
            case 3:
                gameType = "Watching";
                break;
            default:
                gameType = "Playing";
                break;
        }

        return gameType + " " + g.getName();
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time The time that the bot has been running for
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time) {
        return getUptime(time, false);
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time     The time that the bot has been running for
     * @param withTime If we should add the seconds, minutes and hours to the time
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time, boolean withTime) {
        /*
        This code has been inspired from JDA-Butler <https://github.com/Almighty-Alpaca/JDA-Butler/>
         */
        //Like it's ever gonna be up for more then a week
        long years = time / 31104000000L;
        long months = time / 2592000000L % 12;
        long days = time / 86400000L % 30;

        //Get the years, months and days
        String uptimeString = "";
        uptimeString += years == 0 ? "" : years + " Year" + (years > 1 ? "s" : "") + ", ";
        uptimeString += months == 0 ? "" : months + " Month" + (months > 1 ? "s" : "") + ", ";
        uptimeString += days == 0 ? "" : days + " Day" + (days > 1 ? "s" : "");

        //If we want the time added we pass in true
        if (withTime) {
            long hours = time / 3600000L % 24;
            long minutes = time / 60000L % 60;
            long seconds = time / 1000L % 60;

            uptimeString += ", " + (hours == 0 ? "" : hours + " Hour" + (hours > 1 ? "s" : "") + ", ");
            uptimeString += minutes == 0 ? "" : minutes + " Minute" + (minutes > 1 ? "s" : "") + ", ";
            uptimeString += seconds == 0 ? "" : seconds + " Second" + (seconds > 1 ? "s" : "") + " ";
        }

        return uptimeString.startsWith(", ") ? uptimeString.replaceFirst(", ", "") : uptimeString;
    }

    /**
     * Gets a Wolfram|Alpha engine for creating and processing queries
     *
     * @return A possibly-null {@link com.wolfram.alpha.WAEngine Wolfram|Alpha engine} instance configured with the
     * token
     */
    public static WAEngine getWolframEngine(String appId) {
//        String appId = Variables.CONFIG.getString("apis.wolframalpha", "");

        if (appId == null || appId.isEmpty())
            return null;

        WAEngine engine = new WAEngine();

        engine.setAppID(appId);

        engine.setIP("0.0.0.0");
        engine.setLocation("San Francisco");
        engine.setMetric(true);
        engine.setCountryCode("USA");

        return engine;
    }

    /**
     * Stops everything
     */
    public static void stop(DBManager database, AudioUtils audioUtils) {
        try {
            database.getConnManager().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            audioUtils.musicManagers.forEach((a, b) -> {
                if (b.player.getPlayingTrack() != null)
                    b.player.stopTrack();
            });
        } catch (java.util.ConcurrentModificationException ignored) {
        }
        database.getService().shutdown();
    }

    public static TextChannel getLogChannel(long channel, Guild g) {
        return getLogChannel(String.valueOf(channel), g);
    }

    /**
     * This gets the channel from a name or id
     *
     * @param channelId the channel name or id
     * @param guild     the guild to search in
     * @return the channel
     */
    public static TextChannel getLogChannel(String channelId, Guild guild) {
        if (channelId == null || channelId.isEmpty()) return GuildUtils.getPublicChannel(guild);

        TextChannel tc;
        try {
            tc = guild.getTextChannelById(channelId);
        } catch (NumberFormatException e) {
            List<TextChannel> tcl = guild.getTextChannelsByName(channelId, true);
            if (tcl.size() > 0) {
                tc = tcl.get(0);
            } else return null;
        }

        return tc;
    }

    /**
     * This generates a random string withe the specified length
     *
     * @param length the length that the string should be
     * @return the generated string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnpqrstuvwxyz";
        StringBuilder output = new StringBuilder();
        while (output.length() < length) { // length of the random string.
            int index = (int) (ThreadLocalRandom.current().nextFloat() * chars.length());
            output.append(chars.charAt(index));
        }
        return output.toString();
    }

    /**
     * Returns a random string that has 10 chars
     *
     * @return a random string that has 10 chars
     */
    public static String generateRandomString() {
        return generateRandomString(10);
    }

    /**
     * Returns a flipped table
     *
     * @return a flipped table
     */
    public static String flipTable() {
        switch (ThreadLocalRandom.current().nextInt(4)) {
            case 0:
                return "(╯°□°)╯︵┻━┻";
            case 1:
                return "(ノ゜Д゜)ノ︵┻━┻";
            case 2:
                return "(ノಥ益ಥ)ノ︵┻━┻";
            case 3:
                return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
            default:
                return "I CAN'T FLIP THIS TABLE";
        }
    }

    public static PendingRequest<Profile> getUserProfile(String uid) {
        /*
         * badges:
         *
         *   Discord dev: 1
         *   Partner: 2
         *   Nitro: (none, check for premium_since != null)
         *   Hypesquad: 4
         *   Bug Hunter: 8
         */
       /* String url = String.format("%susers/%s/profile", Requester.DISCORD_API_PREFIX, uid);
        return WebUtils.ins.prepareRaw(
                new Request.Builder()
                        .get()
                        .header("authorization", "")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36 OPR/52.0.2871.40")
                        .url(url)
                        .build()
                , b -> {
                    JSONObject json = new JSONObject(Objects.requireNonNull(b).string());
                    JSONObject userObj = json.optJSONObject("user");
                    return new Profile(
                            json.optString("premium_since", null),
                            userObj.optInt("flags"),
                            userObj.optString("id"),
                            userObj.optString("avatar"),
                            userObj.optString("username"),
                            userObj.optString("discriminator")
                    );
                }
        );*/
        return null;
    }

    public static PendingRequest<String> shortenUrl(String url, String googleKey) {
        return WebUtils.ins.shortenUrl(url, googleKey);
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String getSystemUptime() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
//            Process uptimeProc = Runtime.getRuntime().exec("uptime | awk -F'( |,|:)+' '{print $6,$7\",\",$8,\"hours,\",$9,\"minutes\"}'");
            Process uptimeProc = Runtime.getRuntime().exec("uptime");
            BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
            String line = in.readLine();
            if (line != null) {
                Matcher matcher = UNIX_UPTIME_PATTERN.matcher(line);
                if (matcher.find()) {
                    return line;
                }
            }
        }
        return "";
    }
}
