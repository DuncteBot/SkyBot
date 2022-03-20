/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils;

import com.dunctebot.models.settings.GuildSetting;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.database.AbstractDatabase;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.api.Ban;
import ml.duncte123.skybot.objects.api.Mute;
import ml.duncte123.skybot.objects.user.ConsoleUser;
import ml.duncte123.skybot.objects.user.FakeUser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_BAN;

public class ModerationUtils {
    public static final int COLOUR_JOIN = 0x32a852;
    public static final int COLOUR_LEAVE = 0xcf362b;

    private static final String[] PROFESSIONAL_RESPONSES = {
        "WHy?",
        "Nope",
        "LOL no",
        "YoU cAnNoT iNtErAcT wItH ThIs MeMbEr",
        "<a:mmLol3d:394951523836362773>"
    };

    private static final Logger LOG = LoggerFactory.getLogger(ModerationUtils.class);

    private ModerationUtils() {}

    public static boolean canInteract(Member mod, Member target, String action, TextChannel channel) {
        if (mod.equals(target)) {
            sendMsg(channel, PROFESSIONAL_RESPONSES[ThreadLocalRandom.current().nextInt(PROFESSIONAL_RESPONSES.length)]);
            return false;
        }

        if (!mod.canInteract(target)) {
            sendMsg(channel, "You cannot " + action + " this member");
            return false;
        }

        final Member self = mod.getGuild().getSelfMember();

        if (!self.canInteract(target)) {
            sendMsg(channel, "I cannot " + action + " this member, are their roles above mine?");
            return false;
        }

        return true;
    }

    public static void modLog(@Nonnull User mod, @Nonnull User punishedUser, @Nonnull String punishment,
                              @Nullable String reason, @Nullable String time, DunctebotGuild guild) {
        if (!isLogEnabled(punishment, guild)) {
            return;
        }

        String length = "";

        if (time != null && !time.isEmpty()) {
            length = " lasting **" + time + "**";
        }

        String reasonLine = "";

        if (reason != null && !reason.isEmpty()) {
            reasonLine = " with reason _\"" + reason + "\"_";
        }

        final MessageConfig.Builder config = new MessageConfig.Builder()
            .setMessage(String.format("User **%#s** got **%s** by **%#s**%s%s",
                punishedUser,
                punishment,
                mod,
                length,
                reasonLine
            ));

        modLog(config, guild);
    }

    public static void modLog(String message, DunctebotGuild guild) {
        modLog(
            new MessageConfig.Builder().setMessage(message),
            guild
        );
    }

    public static void modLog(MessageConfig.Builder message, DunctebotGuild guild) {
        final long channel = guild.getSettings().getLogChannel();

        if (channel > 0) {
            final TextChannel logChannel = AirUtils.getLogChannel(channel, guild);

            if (logChannel == null) {
                return;
            }

            sendMsg(message.setChannel(logChannel).build());
        }
    }

    private static boolean isLogEnabled(String type, DunctebotGuild guild) {
        final GuildSetting settings = guild.getSettings();

        return switch (type) {
            case "ban", "banned", "softban" -> settings.isBanLogging();
            case "unban", "unbanned" -> settings.isUnbanLogging();
            case "mute", "muted", "unmuted" -> settings.isMuteLogging();
            case "kick", "kicked" -> settings.isKickLogging();
            case "warn", "warned" -> settings.isWarnLogging();
            default -> true;
        };
    }

    public static void handleUnmute(List<Mute> mutes, AbstractDatabase adapter, Variables variables) throws ExecutionException, InterruptedException {
        LOG.debug("Checking for users to unmute");
        final ShardManager shardManager = SkyBot.getInstance().getShardManager();

        for (final Mute mute : mutes) {
            final Guild guild = shardManager.getGuildById(mute.getGuildId());

            if (guild == null) {
                continue;
            }

            Member target = null;

            try {
                target = guild.retrieveMemberById(mute.getUserId()).complete();
            } catch (ErrorResponseException e) {
                // if it's not unknown member or unknown user we will rethroww the exception
                if (e.getErrorResponse() != ErrorResponse.UNKNOWN_MEMBER
                    && e.getErrorResponse() != ErrorResponse.UNKNOWN_USER) {
                    throw e;
                }
            }

            if (target == null) {
                continue;
            }

            if (!guild.getSelfMember().canInteract(target)) {
                continue;
            }

            final User targetUser = target.getUser();

            LOG.debug("Unmuting " + mute.getUserTag());

            final DunctebotGuild dbGuild = new DunctebotGuild(guild, variables);
            final long muteRoleId = dbGuild.getSettings().getMuteRoleId();

            if (muteRoleId < 1L) {
                continue;
            }

            final Role muteRole = guild.getRoleById(muteRoleId);

            if (muteRole == null) {
                continue;
            }

            if (!guild.getSelfMember().canInteract(muteRole)) {
                continue;
            }

            guild.removeRoleFromMember(target, muteRole).reason("Mute expired").queue();

            modLog(new ConsoleUser(), targetUser, "unmuted", null, null, dbGuild);
        }

        LOG.debug("Checking done, unmuted {} users.", mutes.size());

        final List<Integer> purgeIds = mutes.stream().map(Mute::getId).collect(Collectors.toList());

        if (!purgeIds.isEmpty()) {
            adapter.purgeMutes(purgeIds).get();
        }
    }

    public static void handleUnban(List<Ban> bans, AbstractDatabase adapter, Variables variables) throws ExecutionException, InterruptedException {
        LOG.debug("Checking for users to unban");

        // Get the ShardManager from our instance
        // Via the ShardManager we can fetch guilds and unban users
        final ShardManager shardManager = SkyBot.getInstance().getShardManager();

        // Loop over all the bans that came in
        for (final Ban ban : bans) {
            // Get the guild from the ban object
            final Guild guild = shardManager.getGuildById(ban.getGuildId());

            // If we're not in the guild anymore just ignore this iteration
            if (guild == null) {
                continue;
            }

            LOG.debug("Unbanning " + ban.getUserName());

            // Unban the user and set the reason to "Ban expired"
            guild.unban(ban.getUserId())
                .reason("Ban expired")
                // Ignore errors that indicate an unknown ban
                // This may happen some times
                .queue(null, ignore(UNKNOWN_BAN));

            // We're creating a fake user even though we can probably get a real user
            // This is to make sure that we have the data we need when logging the unban
            final User fakeUser = new FakeUser(
                ban.getUserName(),
                Long.parseUnsignedLong(ban.getUserId()),
                Short.parseShort(ban.getDiscriminator())
            );

            // Send the unban to the log channel
            modLog(new ConsoleUser(), fakeUser, "unbanned", null, null, new DunctebotGuild(guild, variables));
        }

        LOG.debug("Checking done, unbanned {} users.", bans.size());

        // Map all the bans to just the id
        final List<Integer> purgeIds = bans.stream().map(Ban::getId).collect(Collectors.toList());

        // If the bans are not empty send a purge request to the databse
        // This will make sure that we don't get them again
        if (!purgeIds.isEmpty()) {
            adapter.purgeBans(purgeIds).get();
        }
    }

    public static void muteUser(DunctebotGuild guild, Member member, TextChannel channel, String cause, long minuteDuration, boolean sendMessages) {
        final GuildSetting guildSettings = guild.getSettings();
        final long muteRoleId = guildSettings.getMuteRoleId();

        if (muteRoleId <= 0) {
            if (sendMessages) {
                sendMsg(channel, "The role for the punished people is not configured. Please set it up." +
                    "We disabled your spam filter until you have set up a role.");
            }

            guild.setSettings(guildSettings.setEnableSpamFilter(false));
            return;
        }

        final Role muteRole = guild.getRoleById(muteRoleId);

        if (muteRole == null) {
            if (sendMessages) {
                sendMsg(channel, "The role for the punished people is nonexistent.");
            }
            return;
        }

        final Member self = guild.getSelfMember();

        if (!self.hasPermission(Permission.MANAGE_ROLES)) {
            if (sendMessages) {
                sendMsg(channel, "I don't have permissions for muting a person. Please give me role managing permissions.");
            }
            return;
        }

        if (!self.canInteract(member) || !self.canInteract(muteRole)) {
            if (sendMessages) {
                sendMsg(channel, "I can not access either the member or the role.");
            }
            return;
        }
        final String reason = String.format("The member %#s was muted for %s until %d", member.getUser(), cause, minuteDuration);
        guild.addRoleToMember(member, muteRole).reason(reason).queue(
            (success) ->
                guild.removeRoleFromMember(member, muteRole).reason("Scheduled un-mute")
                    .queueAfter(minuteDuration, TimeUnit.MINUTES)
            ,
            (failure) -> {
                final long chan = guildSettings.getLogChannel();
                if (chan > 0) {
                    final TextChannel logChannel = AirUtils.getLogChannel(chan, guild);

                    if (logChannel == null) {
                        return;
                    }

                    final String message = String.format("%#s bypassed the mute.", member.getUser());

                    if (sendMessages) {
                        sendMsg(new MessageConfig.Builder()
                            .setChannel(logChannel)
                            .addEmbed(embedMessage(message))
                            .build());
                    }
                }
            });
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause, boolean sendMessages) {
        final Member self = guild.getSelfMember();

        if (!self.hasPermission(Permission.KICK_MEMBERS)) {
            if (sendMessages) {
                sendMsg(channel, "I don't have permissions for kicking a person. Please give me kick members permissions.");
            }
            return;
        }

        if (!self.canInteract(member)) {
            if (sendMessages) {
                sendMsg(channel, "I can not access the member.");
            }
            return;
        }
        final String reason = String.format("The member %#s was kicked for %s.", member.getUser(), cause);
        guild.kick(member, reason).reason(reason).queue();
    }
}
