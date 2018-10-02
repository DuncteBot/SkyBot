/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
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
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, DunctebotGuild g) {
        long chan = g.getSettings().getLogChannel();
        if (chan > 0) {
            TextChannel logChannel = AirUtils.getLogChannel(chan, g);
            String length = "";
            if (time != null && !time.isEmpty()) {
                length = " lasting " + time + "";
            }

            sendMsg(logChannel, String.format("User **%#s** got **%s** by **%#s**%s%s",
                punishedUser,
                punishment,
                mod,
                length,
                reason.isEmpty() ? "" : " with reason _\"" + reason + "\"_"
            ));
        }
    }

    /**
     * A version of {@link #modLog(User, User, String, String, String, DunctebotGuild)} but without the time
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, DunctebotGuild g) {
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
    public static void modLog(User mod, User unbannedUser, String punishment, DunctebotGuild g) {
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
    public static void addBannedUserToDb(DBManager database, String modID, String userName, String userDiscriminator, String userId, String unbanDate, String guildId) {

        database.run(() -> {
            Connection conn = database.getConnManager().getConnection();
            try {
                PreparedStatement smt = conn.prepareStatement("INSERT INTO bans(modUserId, Username, discriminator, userId, ban_date, unban_date, guildId) " +
                    "VALUES(? , ? , ? , ? , NOW() , ?, ?)");

                smt.setString(1, modID);
                smt.setString(2, userName);
                smt.setString(3, userDiscriminator);
                smt.setString(4, userId);
                smt.setString(5, unbanDate);
                smt.setString(6, guildId);
                smt.execute();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns the current amount of warnings that a user has
     *
     * @param u the {@link User User} to check the warnings for
     * @return The current amount of warnings that a user has
     */
    public static int getWarningCountForUser(DBManager database, User u, Guild g) {
        if (u == null)
            throw new IllegalArgumentException("User to check can not be null");

        return ApiUtils.getWarnsForUser(database, u.getId(), g.getId()).getWarnings().size();
    }

    /**
     * This attempts to register a warning in the database
     *
     * @param moderator The mod that executed the warning
     * @param target    The user to warn
     * @param reason    the reason for the warn
     */
    public static void addWarningToDb(DBManager database, User moderator, User target, String reason, Guild guild) {

        database.run(() -> {
            Connection conn = database.getConnManager().getConnection();
            try {
                PreparedStatement smt = conn.prepareStatement("INSERT INTO warnings(mod_id, user_id, reason, guild_id, warn_date, expire_date) " +
                    "VALUES(? , ? , ? , ?  , CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY) )");
                smt.setString(1, moderator.getId());
                smt.setString(2, target.getId());
                smt.setString(3, reason);
                smt.setString(4, guild.getId());
                smt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This will check if there are users that can be unbanned
     */
    public static void checkUnbans(Variables variables) {
        DBManager database = variables.getDatabase();
        database.run(() -> {
            ShardManager shardManager = SkyBot.getInstance().getShardManager();
            logger.debug("Checking for users to unban");
            int usersUnbanned = 0;
            Connection connection = database.getConnManager().getConnection();

            try {

                Statement smt = connection.createStatement();

                ResultSet res = smt.executeQuery("SELECT * FROM " + database.getName() + ".bans");

                while (res.next()) {
                    Date unbanDate = res.getTimestamp("unban_date");
                    Date currDate = new Date();

                    if (currDate.after(unbanDate)) {
                        usersUnbanned++;
                        String username = res.getString("Username");
                        logger.debug("Unbanning " + username);
                        try {
                            String guildId = res.getString("guildId");
                            String userID = res.getString("userId");
                            Guild guild = shardManager.getGuildById(guildId);
                            if (guild != null) {
                                guild.getController()
                                    .unban(userID).reason("Ban expired").queue();
                                modLog(new ConsoleUser(),
                                    new FakeUser(username,
                                        Long.parseUnsignedLong(userID),
                                        Short.valueOf(res.getString("discriminator"))),
                                    "unbanned",
                                    new DunctebotGuild(guild, variables)
                                );
                            }
                        } catch (NullPointerException ignored) {
                        }
                        connection.createStatement().executeUpdate("DELETE FROM " + database.getName() + ".bans WHERE id=" + res.getInt("id") + "");
                    }
                }
                logger.debug("Checking done, unbanned " + usersUnbanned + " users.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    public static void muteUser(DunctebotGuild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute) {
        muteUser(guild, member, channel, cause, minutesUntilUnMute, false);
    }

    public static void muteUser(DunctebotGuild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute, boolean sendMessages) {
        Member self = guild.getSelfMember();
        GuildSettings guildSettings = guild.getSettings();
        long muteRoleId = guildSettings.getMuteRoleId();

        if (muteRoleId <= 0) {
            if (sendMessages)
                sendMsg(channel, "The role for the punished people is not configured. Please set it up." +
                    "We disabled your spam filter until you have set up a role.");

            guildSettings.setEnableSpamFilter(false);
            return;
        }

        Role muteRole = guild.getRoleById(muteRoleId);

        if (muteRole == null) {
            if (sendMessages)
                sendMsg(channel, "The role for the punished people is inexistent.");
            return;
        }

        if (!self.hasPermission(Permission.MANAGE_ROLES)) {
            if (sendMessages)
                sendMsg(channel, "I don't have permissions for muting a person. Please give me role managing permissions.");
            return;
        }

        if (!self.canInteract(member) || !self.canInteract(muteRole)) {
            if (sendMessages)
                sendMsg(channel, "I can not access either the member or the role.");
            return;
        }
        String reason = String.format("The member %#s was muted for %s until %d", member.getUser(), cause, minutesUntilUnMute);
        guild.getController().addSingleRoleToMember(member, muteRole).reason(reason).queue(
            (success) ->
                guild.getController().removeSingleRoleFromMember(member, muteRole).reason("Scheduled un-mute")
                    .queueAfter(minutesUntilUnMute, TimeUnit.MINUTES)
            ,
            (failure) -> {
                long chan = guildSettings.getLogChannel();
                if (chan > 0) {
                    TextChannel logChannel = AirUtils.getLogChannel(chan, guild);

                    String message = String.format("%#s bypassed the mute.", member.getUser());

                    if (sendMessages)
                        MessageUtils.sendEmbed(logChannel, EmbedUtils.embedMessage(message));
                }
            });
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause) {
        kickUser(guild, member, channel, cause, false);
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause, boolean sendMessages) {
        Member self = guild.getSelfMember();

        if (!self.hasPermission(Permission.KICK_MEMBERS)) {
            if (sendMessages)
                sendMsg(channel, "I don't have permissions for kicking a person. Please give me kick members permissions.");
            return;
        }

        if (!self.canInteract(member)) {
            if (sendMessages)
                sendMsg(channel, "I can not access the member.");
            return;
        }
        String reason = String.format("The member %#s was kicked for %s.", member.getUser(), cause);
        guild.getController().kick(member).reason(reason).queue();
    }
}
