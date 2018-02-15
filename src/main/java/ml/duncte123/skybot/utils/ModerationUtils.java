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
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ModerationUtils {

    private static Logger logger = LoggerFactory.getLogger(ModerationUtils.class);

    /**
     * This will send a message to a channel called modlog
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param time         How long it takes for the punishment to get removed
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, Guild g){
        TextChannel logChannel = AirUtils.getLogChannel(GuildSettingsUtils.getGuild(g).getLogChannel(), g);
        String length = "";
        if (time != null && !time.isEmpty()) {
            length = " lasting " + time + "";
        }

        MessageUtils.sendMsg(logChannel, String.format("User **%s** got **%s** by **%s**%s%s",
                String.format("%#s", punishedUser),
                punishment,
                String.format("%#s", mod),
                length,
                reason.isEmpty() ? "" : " with reason _\"" + reason + "\"_"
        ));
    }

    /**
     * A version of {@link #modLog(User, User, String, String, String, Guild)} but without the time
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, Guild g) {
        modLog(mod, punishedUser, punishment, reason, "", g);
    }

    /**
     * To log a unban or a unmute
     *
     * @param mod          The mod that permed the executeCommand
     * @param unbannedUser The user that the executeCommand is for
     * @param punishment   The type of punishment that got removed
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, Guild g) {
        modLog(mod, unbannedUser, punishment, "", g);
    }

    /**
     * Add the banned user to the database
     *
     * @param modID             The user id from the mod
     * @param userName          The username from the banned user
     * @param userDiscriminator the discriminator from the user
     * @param userId            the id from the banned users
     * @param unbanDate         When we need to unban the user
     * @param guildId           What guild the user got banned in
     */
    public static void addBannedUserToDb(String modID, String userName, String userDiscriminator, String userId, String unbanDate, String guildId) {
        Map<String, Object> postFields = new TreeMap<>();
        postFields.put("modId", modID);
        postFields.put("username", userName);
        postFields.put("discriminator", userDiscriminator);
        postFields.put("userId", userId);
        postFields.put("unbanDate", unbanDate);
        postFields.put("guildId", guildId);

        try {
            WebUtils.postRequest(Settings.apiBase + "/ban/json", postFields, WebUtils.AcceptType.URLENCODED);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the current amount of warnings that a user has
     * @param u the {@link User User} to check the warnings for
     * @return The current amount of warnings that a user has
     */
    public static int getWarningCountForUser(User u, Guild g) {
        if(u == null)
            throw new IllegalArgumentException("User to check can not be null");
        try {
            return WebUtils.getJSONObject(String.format(
                    "%s/getWarnsForUser/json?user_id=%s&guild_id=%s",
                    Settings.apiBase,
                    u.getId(),
                    g.getId())).getJSONArray("warnings").length();
        }
        catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * This attempts to register a warning in the database
     * @param moderator The mod that executed the warning
     * @param target The user to warn
     * @param reason the reason for the warn
     * @param jda a jda instance because we need the token for auth
     */
    public static void addWarningToDb(User moderator, User target, String reason, Guild guild, JDA jda) {
        Map<String, Object> postFields = new HashMap<>();
        postFields.put("mod_id", moderator.getId());
        postFields.put("user_id", target.getId());
        postFields.put("guild_id", guild.getId());
        postFields.put("reason", reason.isEmpty()? "No Reason provided" : " for " + reason);
        postFields.put("token", jda.getToken());

        try {
            WebUtils.postRequest(Settings.apiBase + "/addWarning/json", postFields, WebUtils.AcceptType.URLENCODED);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will check if there are users that can be unbanned
     *
     * @param shardManager the current shard manager for this bot
     */
    public static void checkUnbans(ShardManager shardManager) {
        logger.debug("Checking for users to unban");
        int usersUnbanned = 0;
        Connection database = AirUtils.DB.getConnManager().getConnection();

        try {

            Statement smt = database.createStatement();

            ResultSet res = smt.executeQuery("SELECT * FROM " + AirUtils.DB.getName() + ".bans");

            while (res.next()) {
                java.util.Date unbanDate = res.getTimestamp("unban_date");
                java.util.Date currDate = new java.util.Date();

                if (currDate.after(unbanDate)) {
                    usersUnbanned++;
                    logger.debug("Unbanning " + res.getString("Username"));
                    try {
                        shardManager.getGuildCache().getElementById(res.getString("guildId")).getController()
                                .unban(res.getString("userId")).reason("Ban expired").queue();
                        modLog(new ConsoleUser(),
                                new FakeUser(res.getString("Username"),
                                        res.getString("userId"),
                                        res.getString("discriminator")),
                                "unbanned",
                                shardManager.getGuildById(res.getString("guildId")));
                    } catch (NullPointerException ignored) { }
                    database.createStatement().executeUpdate("DELETE FROM " + AirUtils.DB.getName() + ".bans WHERE id=" + res.getInt("id") + "");
                }
            }
            logger.debug("Checking done, unbanned " + usersUnbanned + " users.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }
}
