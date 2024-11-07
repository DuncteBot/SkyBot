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

package me.duncte123.skybot.listeners;

import com.dunctebot.models.settings.GuildSetting;
import com.jagrosh.jagtag.Parser;
import kotlin.Pair;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.skybot.Settings;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.database.AbstractDatabase;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.extensions.Time4JKt;
import me.duncte123.skybot.extensions.UserKt;
import me.duncte123.skybot.objects.GuildMemberInfo;
import me.duncte123.skybot.utils.CommandUtils;
import me.duncte123.skybot.utils.GuildSettingsUtils;
import me.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.utils.TimeFormat;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.skybot.utils.ModerationUtils.*;
import static net.dv8tion.jda.api.requests.ErrorResponse.*;

public class GuildMemberListener extends BaseListener {

    public GuildMemberListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildMemberJoinEvent memberJoin) {
            this.onGuildMemberJoin(memberJoin);
        } else if (event instanceof GuildMemberUpdatePendingEvent pendingUpdate) {
            this.onGuildMemberUpdatePending(pendingUpdate);
        } else if (event instanceof GuildMemberRemoveEvent memberRemove) {
            this.onGuildMemberRemove(memberRemove);
        } else if (event instanceof GuildMemberRoleRemoveEvent roleRemove) {
            this.onGuildMemberRoleRemove(roleRemove);
        } else if (event instanceof GuildMemberRoleAddEvent roleAdd) {
            this.onGuildMemberRoleAdd(roleAdd);
        } else if (event instanceof GuildLeaveEvent guildLeave) {
            final Guild guild = guildLeave.getGuild();
            final long guildId = guild.getIdLong();

            // invites are cleared by the invite listener
            GuildUtils.GUILD_MEMBER_COUNTS.remove(guildId);
            variables.getGuildSettingsCache().remove(guildId);
        }
    }

    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final Member selfMember = guild.getSelfMember();

        if (member.equals(selfMember)) {
            return;
        }

        final GuildSetting settings = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);

        if (settings.isYoungAccountBanEnabled() && bannedAccount(event, guild, member, selfMember, settings)) {
            return;
        }

        updateGuildCount(event, guild);

        if (settings.isMemberLogging()) {
            logMemberNotification(event.getUser(), guild, "Join", "joined", COLOUR_JOIN);
        }

        if (settings.isEnableJoinMessage() && settings.getWelcomeLeaveChannel() > 0) {
            final long channelId = settings.getWelcomeLeaveChannel();

            final TextChannel channel = guild.getTextChannelById(channelId);
            final String msg = parseGuildVars(settings.getCustomJoinMessage(), event);

            if (!msg.isEmpty() && !"".equals(msg.trim()) && channel != null) {
                sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(channel)
                        .setMessage(msg)
                );
            }
        }

        if (settings.isAutoroleEnabled() && !member.isPending() && selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            applyAutoRole(guild, member, settings);
        }
    }

    private void onGuildMemberUpdatePending(GuildMemberUpdatePendingEvent event) {
        final Member member = event.getMember();

        // only apply the role if the member is not pending
        if (!member.isPending()) {
            final Guild guild = event.getGuild();
            final Member selfMember = guild.getSelfMember();
            final GuildSetting settings = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);

            if (settings.isAutoroleEnabled() && selfMember.hasPermission(Permission.MANAGE_ROLES)) {
                applyAutoRole(guild, member, settings);
            }
        }
    }

    private void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        final User user = event.getUser();
        final SelfUser selfUser = event.getJDA().getSelfUser();

        // If we are leaving we need to ignore this as we cannot send messages to any channels
        // when this event is fired
        if (user.equals(selfUser)) {
            return;
        }

        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final GuildMemberInfo guildCounts = GuildUtils.GUILD_MEMBER_COUNTS.get(guildId);

        if (guildCounts != null) {
            if (user.isBot()) {
                guildCounts.bots -= 1;
            } else {
                guildCounts.users -= 1;

                final String avatarId = user.getAvatarId();

                if (avatarId != null && avatarId.startsWith("a_")) {
                    guildCounts.nitroUsers -= 1;
                }
            }
        }

        final GuildSetting settings = GuildSettingsUtils.getGuild(guildId, this.variables);

        if (settings.isMemberLogging()) {
            logMemberNotification(user, guild, "Leave", "left", COLOUR_LEAVE);
        }

        // If the leave message is enabled and we have a welcome channel
        if (settings.isEnableLeaveMessage() && settings.getWelcomeLeaveChannel() > 0) {
            final long channelId = settings.getWelcomeLeaveChannel();

            final TextChannel channel = guild.getTextChannelById(channelId);
            final String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);

            // If we have a message and the text channel is not null
            if (!msg.isEmpty() && !msg.trim().isEmpty() && channel != null) {
                sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(channel)
                        .setMessage(msg)
                );
            }
        }

        if (guildId == Settings.SUPPORT_GUILD_ID) {
            handlePatronRemoval(user.getIdLong(), event.getJDA());
        }
    }

    @Deprecated
    private void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        //
    }

    @Deprecated
    private void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        //
    }

    private void logMemberNotification(User user, Guild guild, String titlePart, String bodyPart, int colour) {
        final Pair<String, String> created = Time4JKt.parseTimeCreated(user);
        final EmbedBuilder embed = new EmbedBuilder()
            .setColor(colour)
            .setThumbnail(UserKt.getStaticAvatarUrl(user))
            .setTitle(titlePart + " Notification", "https://duncte.bot/")
            .setDescription(String.format(
                "%s (`%s`/%s) has %s the server!",
                user.getAsMention(),
                user.getAsTag(),
                user.getIdLong(),
                bodyPart
            ))
            .addField("Account created", created.getFirst() + '\n' + created.getSecond(), false);

        final Consumer<Void> sendLog = (ignored) -> modLog(
            new MessageConfig.Builder().addEmbed(true, embed),
            new DunctebotGuild(guild, this.variables)
        );

        if (user.isBot() && guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS) && "Join".equals(titlePart)) {
            // fetch who added the bot from the audit logs
            guild.retrieveAuditLogs()
                .cache(false)
                .type(ActionType.BOT_ADD)
                .limit(10)
                .queue((logs) -> {
                    if (!logs.isEmpty()) {
                        final Optional<AuditLogEntry> optionalEntry = logs.stream()
                            .filter((log) -> log.getTargetIdLong() == user.getIdLong())
                            .findFirst();

                        if (optionalEntry.isPresent()) {
                            final AuditLogEntry entry = optionalEntry.get();

                            embed.appendDescription(String.format(
                                "\nThis bot was added by **%#s**",
                                entry.getUser()
                            ));
                        }
                    }

                    sendLog.accept(null);
                });
            return;
        }

        sendLog.accept(null);
    }

    private void updateGuildCount(GuildMemberJoinEvent event, Guild guild) {
        final GuildMemberInfo guildCounts = GuildUtils.GUILD_MEMBER_COUNTS.get(guild.getIdLong());

        if (guildCounts != null) {
            final User user = event.getUser();

            if (user.isBot()) {
                guildCounts.bots += 1;
            } else {
                guildCounts.users += 1;

                final String avatarId = user.getAvatarId();

                if (avatarId != null && avatarId.startsWith("a_")) {
                    guildCounts.nitroUsers += 1;
                }
            }
        }
    }

    private boolean bannedAccount(GuildMemberJoinEvent event, Guild guild, Member member, Member selfMember, GuildSetting settings) {
        final User user = event.getUser();
        final OffsetDateTime timeCreated = user.getTimeCreated();

        final Duration between = Duration.between(timeCreated, OffsetDateTime.now());
        final long daysBetween = between.toDays();
        final int threshold = settings.getYoungAccountThreshold();

        if (daysBetween < threshold && selfMember.hasPermission(Permission.BAN_MEMBERS) && selfMember.canInteract(member)) {
            final CompletableFuture<Boolean> booleanFuture = new CompletableFuture<>();
            final AbstractDatabase database = variables.getDatabase();
            final String humanTime = TimeFormat.RELATIVE.format(timeCreated);

            // we have to use futures since the callback runs on a different thread
            database.getBanBypass(guild.getIdLong(), member.getIdLong()).thenAccept((byPass) -> {
                if (byPass != null) {
                    // delete the bypass as it is used
                    database.deleteBanBypass(byPass);
                    // return false, we did not ban (does not block any welcome messages from displaying)
                    booleanFuture.complete(false);

                    modLog(
                        String.format("User **%#s** bypassed the auto ban with a manually set bypass (created %s)",
                            member.getUser(),
                            humanTime
                        ),
                        new DunctebotGuild(guild, this.variables)
                    );
                    return;
                }

                // return true, we did ban the user (prevents any welcome messages from displaying)
                booleanFuture.complete(true);

                final String reason = "Account is newer than " + threshold + " days (created " + humanTime + ')';

                guild.ban(member, 0, TimeUnit.DAYS)
                    .reason(reason)
                    .queue();

                modLog(
                    selfMember.getUser(),
                    member.getUser(),
                    "banned",
                    reason,
                    null,
                    new DunctebotGuild(guild, this.variables)
                );
            });

            try {
                return booleanFuture.get();
            }
            catch (InterruptedException | ExecutionException ignored) { // should never happen
                return false;
            }
        }

        return false;
    }

    @Nonnull
    private String parseGuildVars(String rawMessage, GenericGuildEvent event) {

        if (!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberRemoveEvent)) {
            return "This code should never run";
        }

        if (rawMessage == null || "".equals(rawMessage.trim())) {
            return "";
        }

        final User user;

        if (event instanceof GuildMemberJoinEvent) {
            user = ((GuildMemberJoinEvent) event).getUser();
        } else {
            user = ((GuildMemberRemoveEvent) event).getUser();
        }

        final Guild guild = event.getGuild();
        final GuildSetting setting = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);
        final long welcomeChannel = setting.getWelcomeLeaveChannel();
        final Parser parser = CommandUtils.PARSER_SUPPLIER.get();

        final String message = parser.put("user", user)
            .put("guild", event.getGuild())
            .put("channel", event.getGuild().getTextChannelById(welcomeChannel))
            .put("args", "")
            .parse(rawMessage);

        parser.clear();

        return message;
    }

    @Deprecated
    private void handlePatronRemoval(long userId, JDA jda) {
        //
    }

    @Deprecated
    private void handleNewOneGuildPatron(long userId) {
       //
    }

    private void applyAutoRole(Guild guild, Member member, GuildSetting settings) {
        final Role role = guild.getRoleById(settings.getAutoroleRole());

        if (role != null && !guild.getPublicRole().equals(role) && guild.getSelfMember().canInteract(role)) {
            guild.addRoleToMember(member, role)
                .queue(null, new ErrorHandler().ignore(UNKNOWN_ROLE, UNKNOWN_MEMBER, MISSING_PERMISSIONS));
        }
    }
}
