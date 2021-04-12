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

package ml.duncte123.skybot.listeners;

import com.dunctebot.models.settings.GuildSetting;
import com.jagrosh.jagtag.Parser;
import kotlin.Pair;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.EventManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.extensions.Time4JKt;
import ml.duncte123.skybot.extensions.UserKt;
import ml.duncte123.skybot.objects.GuildMemberInfo;
import ml.duncte123.skybot.objects.api.AllPatronsData;
import ml.duncte123.skybot.objects.api.Patron;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.time4j.format.TextWidth;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.Settings.PATREON;
import static ml.duncte123.skybot.utils.ModerationUtils.*;
import static net.dv8tion.jda.api.requests.ErrorResponse.*;

public class GuildMemberListener extends BaseListener {

    public GuildMemberListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildMemberJoinEvent) {
            this.onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildMemberUpdatePendingEvent) {
            this.onGuildMemberUpdatePending((GuildMemberUpdatePendingEvent) event);
        } else if (event instanceof GuildMemberRemoveEvent) {
            this.onGuildMemberRemove((GuildMemberRemoveEvent) event);
        } else if (event instanceof GuildMemberRoleRemoveEvent) {
            this.onGuildMemberRoleRemove((GuildMemberRoleRemoveEvent) event);
        } else if (event instanceof GuildMemberRoleAddEvent) {
            this.onGuildMemberRoleAdd((GuildMemberRoleAddEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            final Guild guild = ((GuildLeaveEvent) event).getGuild();
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
                sendMsg(channel, msg);
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
        final GuildMemberInfo guildCounts = GuildUtils.GUILD_MEMBER_COUNTS.getIfPresent(guildId);

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
            if (!msg.isEmpty() && !"".equals(msg.trim()) && channel != null) {
                sendMsg(channel, msg);
            }
        }

        if (guildId == Settings.SUPPORT_GUILD_ID) {
            handlePatronRemoval(user.getIdLong(), event.getJDA());
        }
    }

    private void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (event.getGuild().getIdLong() != Settings.SUPPORT_GUILD_ID) {
            return;
        }

        final boolean patronRemoved = event.getRoles()
            .stream()
            .map(Role::getIdLong)
            .anyMatch(
                (roleId) -> roleId == Settings.PATRONS_ROLE || roleId == Settings.TAG_PATRONS_ROLE ||
                    roleId == Settings.GUILD_PATRONS_ROLE || roleId == Settings.ONE_GUILD_PATRONS_ROLE
            );

        if (patronRemoved) {
            handlePatronRemoval(event.getUser().getIdLong(), event.getJDA());
        }
    }

    private void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (event.getGuild().getIdLong() != Settings.SUPPORT_GUILD_ID) {
            return;
        }

        final long userId = event.getUser().getIdLong();
        final AtomicReference<Patron.Type> typeToSet = new AtomicReference<>(null);

        event.getRoles()
            .stream()
            .map(Role::getIdLong)
            .forEach((roleId) -> {
                // All guild patron
                if (roleId == Settings.GUILD_PATRONS_ROLE) {
                    CommandUtils.GUILD_PATRONS.add(userId);
                    typeToSet.set(Patron.Type.ALL_GUILD);
                    return;
                }

                // One guild patron
                if (roleId == Settings.ONE_GUILD_PATRONS_ROLE) {
                    CommandUtils.PATRONS.remove(userId);
                    handleNewOneGuildPatron(userId);
                    // We assume that the patron already did the steps to register
                    typeToSet.set(null);
                    return;
                }

                // Tag patron
                if (roleId == Settings.TAG_PATRONS_ROLE) {
                    CommandUtils.PATRONS.remove(userId);
                    CommandUtils.TAG_PATRONS.add(userId);
                    typeToSet.set(Patron.Type.TAG);
                    return;
                }

                // Normal patron
                if (roleId == Settings.PATRONS_ROLE) {
                    CommandUtils.PATRONS.add(userId);
                    typeToSet.set(Patron.Type.NORMAL);
                }
            });

        // if we have a type set it in the database
        // Type is set in the database here to prevent un-needed updates
        if (typeToSet.get() != null) {
            variables.getDatabaseAdapter().createOrUpdatePatron(typeToSet.get(), userId, null);
        }
    }

    private void logMemberNotification(User user, Guild guild, String titlePart, String bodyPart, int colour) {
        final Pair<String, String> created = Time4JKt.parseTimeCreated(user);
        final EmbedBuilder embed = new EmbedBuilder()
            .setColor(colour)
            .setThumbnail(UserKt.getStaticAvatarUrl(user))
            .setTitle(titlePart + " Notification", PATREON)
            .setDescription(String.format(
                "%s (`%s`/%s) has %s the server!",
                user.getAsMention(),
                user.getAsTag(),
                user.getIdLong(),
                bodyPart
            ))
            .addField("Account created", created.getFirst() + '\n' + created.getSecond(), false);

        final Consumer<Void> sendLog = (ignored) -> modLog(
            new MessageConfig.Builder().setEmbed(embed, true),
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
        final GuildMemberInfo guildCounts = GuildUtils.GUILD_MEMBER_COUNTS.getIfPresent(guild.getIdLong());

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
            final DatabaseAdapter database = variables.getDatabaseAdapter();
            final String humanTime = Time4JKt.humanize(timeCreated, TextWidth.ABBREVIATED);

            // we have to use futures since the callback runs on a different thread
            database.getBanBypass(guild.getIdLong(), member.getIdLong(), (byPass) -> {
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
                    return null;
                }

                // return true, we did ban the user (prevents any welcome messages from displaying)
                booleanFuture.complete(true);

                final String reason = "Account is newer than " + threshold + " days (created " + humanTime + ')';

                guild.ban(member, 0, reason)
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

                return null;
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

    private void handlePatronRemoval(long userId, JDA jda) {
        // Remove the user from the patrons list
        final boolean hadNormalRank = CommandUtils.PATRONS.remove(userId);

        // If the main patron role is removed we can just remove the patron from the database
        if (hadNormalRank) {
            variables.getDatabaseAdapter().removePatron(userId);
            return;
        }

        final boolean hadTag = CommandUtils.TAG_PATRONS.remove(userId);
        Patron.Type newType = null;

        if (hadTag) {
            newType = Patron.Type.NORMAL;
        }

        boolean hadOneGuild = false;
        final InviteTrackingListener tracker = ((EventManager) jda.getEventManager()).getInviteTracker();

        if (CommandUtils.ONEGUILD_PATRONS.containsKey(userId)) {
            // Remove the user from the one guild patrons
            final long guildId = CommandUtils.ONEGUILD_PATRONS.remove(userId);

            // invalidate the invite cache for this guild
            tracker.clearInvites(guildId);

            hadOneGuild = true;
        }

        final boolean hadGuildPatron = CommandUtils.GUILD_PATRONS.remove(userId);

        if (hadGuildPatron) {
            // clear the invite cache for all guilds of this user since they aren't a patreon anymore
            // this has to be it's own check or it will do a possibly useless/wrong check for removing logging access
            CommandUtils.getPatronGuildIds(userId, jda.getShardManager())
                .forEach(tracker::clearInvites);
        }

        if (hadOneGuild || hadGuildPatron) {
            newType = Patron.Type.TAG;
        }

        // Remove when null?
        if (newType != null) {
            final Patron patron = new Patron(newType, userId, null);

            variables.getDatabaseAdapter().createOrUpdatePatron(patron);
            CommandUtils.addPatronsFromData(AllPatronsData.fromSinglePatron(patron));
        }
    }

    private void handleNewOneGuildPatron(long userId) {
        variables.getDatabaseAdapter().getOneGuildPatron(userId,
            (results) -> {
                results.forEachEntry(
                    (a, guildId) -> {
                        CommandUtils.ONEGUILD_PATRONS.put(userId, guildId);

                        return true;
                    }
                );

                return null;
            }
        );
    }

    private void applyAutoRole(Guild guild, Member member, GuildSetting settings) {
        final Role role = guild.getRoleById(settings.getAutoroleRole());

        if (role != null && !guild.getPublicRole().equals(role) && guild.getSelfMember().canInteract(role)) {
            guild.addRoleToMember(member, role)
                .queue(null, new ErrorHandler().ignore(UNKNOWN_ROLE, UNKNOWN_MEMBER, MISSING_PERMISSIONS));
        }
    }
}
