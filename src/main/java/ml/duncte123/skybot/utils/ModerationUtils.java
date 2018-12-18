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
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import ml.duncte123.skybot.objects.api.Ban;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class ModerationUtils {

    private static Logger logger = LoggerFactory.getLogger(ModerationUtils.class);

    /**
     * This will send a message to a channel called modlog
     *
     * @param mod
     *         The mod that performed the punishment
     * @param punishedUser
     *         The user that got punished
     * @param punishment
     *         The type of punishment
     * @param reason
     *         The reason of the punishment
     * @param time
     *         How long it takes for the punishment to get removed
     * @param g
     *         A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, DunctebotGuild g) {
        long chan = g.getSettings().getLogChannel();
        if (chan > 0) {
            final TextChannel logChannel = AirUtils.getLogChannel(chan, g);
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
     * @param mod
     *         The mod that performed the punishment
     * @param punishedUser
     *         The user that got punished
     * @param punishment
     *         The type of punishment
     * @param reason
     *         The reason of the punishment
     * @param g
     *         A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, DunctebotGuild g) {
        modLog(mod, punishedUser, punishment, reason, "", g);
    }

    /**
     * To log a unban or a unmute
     *
     * @param mod
     *         The mod that permed the executeCommand
     * @param unbannedUser
     *         The user that the executeCommand is for
     * @param punishment
     *         The type of punishment that got removed
     * @param g
     *         A instance of the {@link Guild}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, DunctebotGuild g) {
        modLog(mod, unbannedUser, punishment, "", g);
    }

    /**
     * Add the banned user to the database
     *
     * @param modID
     *         The user id from the mod
     * @param userName
     *         The username from the banned user
     * @param userDiscriminator
     *         the discriminator from the user
     * @param userId
     *         the id from the banned users
     * @param unbanDate
     *         When we need to unban the user
     * @param guildId
     *         What guild the user got banned in
     */
    public static void addBannedUserToDb(DatabaseAdapter adapter, long modID, String userName, String userDiscriminator, long userId, String unbanDate, long guildId) {
        adapter.createBan(modID, userName, userDiscriminator, userId, unbanDate, guildId);
    }

    /**
     * Returns the current amount of warnings that a user has
     *
     * @param u
     *         the {@link User User} to check the warnings for
     *
     * @return The current amount of warnings that a user has
     */
    public static int getWarningCountForUser(DatabaseAdapter adapter, @NotNull User u, @NotNull Guild g) {
        return ApiUtils.getWarnsForUser(adapter, u.getIdLong(), g.getIdLong()).getWarnings().size();
    }

    /**
     * This attempts to register a warning in the database
     *
     * @param moderator
     *         The mod that executed the warning
     * @param target
     *         The user to warn
     * @param reason
     *         the reason for the warn
     */
    public static void addWarningToDb(DatabaseAdapter adapter, User moderator, User target, String reason, Guild guild) {
        adapter.createWarning(moderator.getIdLong(), target.getIdLong(), guild.getIdLong(), reason);
    }

    /**
     * This will check if there are users that can be unbanned
     */
    public static void checkUnbans(Variables variables) {
        variables.getDatabaseAdapter().getExpiredBans(
            (bans) -> {
                logger.debug("Checking for users to unban");
                final ShardManager shardManager = SkyBot.getInstance().getShardManager();

                for (Ban ban : bans) {
                    Guild guild = shardManager.getGuildById(ban.getGuildId());

                    if (guild == null) {
                        continue;
                    }


                    logger.debug("Unbanning " + ban.getUserName());

                    guild.getController()
                        .unban(ban.getUserId()).reason("Ban expired").queue();

                    modLog(new ConsoleUser(),
                        new FakeUser(ban.getUserName(),
                            Long.parseUnsignedLong(ban.getUserId()),
                            Short.valueOf(ban.getDiscriminator())),
                        "unbanned",
                        new DunctebotGuild(guild, variables)
                    );

                }

                logger.debug("Checking done, unbanned {} users.", bans.size());

                final List<Integer> purgeIds = bans.stream().map(Ban::getId).collect(Collectors.toList());

                variables.getDatabaseAdapter().purgeBans(purgeIds);

                return null;
            }
        );
    }

    public static void muteUser(DunctebotGuild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute) {
        muteUser(guild, member, channel, cause, minutesUntilUnMute, false);
    }

    public static void muteUser(DunctebotGuild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute, boolean sendMessages) {
        final Member self = guild.getSelfMember();
        final GuildSettings guildSettings = guild.getSettings();
        final long muteRoleId = guildSettings.getMuteRoleId();

        if (muteRoleId <= 0) {
            if (sendMessages)
                sendMsg(channel, "The role for the punished people is not configured. Please set it up." +
                    "We disabled your spam filter until you have set up a role.");

            guildSettings.setEnableSpamFilter(false);
            return;
        }

        final Role muteRole = guild.getRoleById(muteRoleId);

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
        final String reason = String.format("The member %#s was muted for %s until %d", member.getUser(), cause, minutesUntilUnMute);
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
                        sendEmbed(logChannel, embedMessage(message));
                }
            });
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause) {
        kickUser(guild, member, channel, cause, false);
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause, boolean sendMessages) {
        final Member self = guild.getSelfMember();

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
        final String reason = String.format("The member %#s was kicked for %s.", member.getUser(), cause);
        guild.getController().kick(member).reason(reason).queue();
    }
}
