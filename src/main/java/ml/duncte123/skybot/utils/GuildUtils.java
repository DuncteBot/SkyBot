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

import ml.duncte123.skybot.Settings;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class GuildUtils {

    private static Logger logger = LoggerFactory.getLogger(GuildUtils.class);

    /**
     * This sends a post request to the bot lists with the new guild count
     *
     * @param jda           the jda instance for the token
     * @param newGuildCount the new guild count
     * @return the response from the server
     */
    public static String updateGuildCount(JDA jda, long newGuildCount) {
        Map<String, Object> postFields = new HashMap<>();
        postFields.put("server_count", newGuildCount);
        postFields.put("auth", jda.getToken());
        try {
            return WebUtils.postRequest(Settings.apiBase + "/postGuildCount/json", postFields, WebUtils.AcceptType.URLENCODED).body().string();
        } catch (NullPointerException ignored) {
            return new JSONObject().put("status", "failure").put("message", "ignored exception").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }


    /**
     * This method updates the guild count and checks it on startup and every time we join or leave a guild.
     * @throws UnsupportedOperationException if the request failed.
     * @param jda the jda
     * @param newGuildCount the new guild count
     */
    public static void updateGuildCountAndCheck(JDA jda, long newGuildCount) {
        JSONObject returnValue = new JSONObject(updateGuildCount(jda, newGuildCount));
        if (returnValue.getString("status").equalsIgnoreCase("failure")) {
            String exceptionMessage = "%s";
            try {
                switch (returnValue.getInt("code")) {
                    case 401: {
                        exceptionMessage = "Unauthorized access! %s";
                        break;
                    }
                    case 400: {
                        exceptionMessage = "Bad request! %s";
                        break;
                    }

                    default: {
                        exceptionMessage = "Server responded with a unknown status message: %s";
                        break;
                    }
                }
            } catch (JSONException ex) {
                String x = returnValue.getString("message");
                if (x.equals("ignored exception"))
                    return;
                throw new UnsupportedOperationException(String.format(exceptionMessage, x), ex);
            }
            String x = returnValue.getString("message");
            if (x.equals("ignored exception"))
                return;
            throw new UnsupportedOperationException(String.format(exceptionMessage, returnValue.getString("message")));
        }
    }

    /**
     * This will calculate the bot to user ratio
     *
     * @param g the {@link Guild} that we want to check
     * @return the percentage of users and the percentage of bots in a nice compact array
     */
    public static double[] getBotRatio(Guild g) {

        MemberCacheView memberCache = g.getMemberCache();
        double totalCount = memberCache.size();
        double botCount = memberCache.stream().filter(it -> it.getUser().isBot()).count();
        double userCount = totalCount - botCount;

        //percent in users
        double userCountP = (userCount / totalCount) * 100;

        //percent in bots
        double botCountP = (botCount / totalCount) * 100;

        logger.debug("In the guild " + g.getName() + "(" + totalCount + " Members), " + userCountP + "% are users, " + botCountP + "% are bots");

        return new double[]{Math.round(userCountP), Math.round(botCountP)};
    }

    /**
     * This counts the users in a guild that have an animated avatar
     * @param g the guild to count it in
     * @return the amount users that have a animated avatar in a {@link java.util.concurrent.atomic.AtomicLong AtomicLong} (because why not)
     */
    public static AtomicLong countAnimatedAvatars(Guild g) {

        return new AtomicLong(g.getMemberCache().stream()
                .map(Member::getUser)
                .filter(it -> it.getAvatarId() != null )
	            .filter(it -> it.getAvatarId().startsWith("a_") ).count()
        );
    }

    /**
     * This will get the first channel of a guild that we can write in/should be able to write in
     *
     * @param guild The guild that we want to get the main channel from
     * @return the Text channel that we can send our messages in.
     */
    public static TextChannel getPublicChannel(Guild guild) {

        TextChannel pubChann = guild.getTextChannelCache().getElementById(guild.getId());

        if (pubChann == null || !pubChann.getGuild().getSelfMember().hasPermission(pubChann, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
            return guild.getTextChannelCache().stream().filter(channel -> guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)).findFirst().orElse(null);
        }

        return pubChann;
    }

    /**
     * This will convert the VerificationLevel from the guild to how it is displayed in the settings
     *
     * @param lvl The level to convert
     * @return The converted verification level
     */
    // Null safety
    public static String verificationLvlToName(Guild.VerificationLevel lvl) {
        if (Guild.VerificationLevel.LOW.equals(lvl)) {
            return "Low";
        } else if (Guild.VerificationLevel.MEDIUM.equals(lvl)) {
            return "Medium";
        } else if (Guild.VerificationLevel.HIGH.equals(lvl)) {
            return "(╯°□°）╯︵ ┻━┻";
        } else if (Guild.VerificationLevel.VERY_HIGH.equals(lvl)) {
            return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
        }
        return "None";
    }
}
