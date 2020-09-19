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

package ml.duncte123.skybot.listeners;

import com.jagrosh.jagtag.Parser;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.objects.GuildMemberInfo;
import ml.duncte123.skybot.objects.api.AllPatronsData;
import ml.duncte123.skybot.objects.api.Patron;
import com.dunctebot.models.settings.GuildSetting;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class GuildMemberListener extends BaseListener {

    public GuildMemberListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildMemberJoinEvent) {
            this.onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildMemberRemoveEvent) {
            this.onGuildMemberRemove((GuildMemberRemoveEvent) event);
        } else if (event instanceof GuildMemberRoleRemoveEvent) {
            this.onGuildMemberRoleRemove((GuildMemberRoleRemoveEvent) event);
        } else if (event instanceof GuildMemberRoleAddEvent) {
            this.onGuildMemberRoleAdd((GuildMemberRoleAddEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            final Guild guild = ((GuildLeaveEvent) event).getGuild();
            final long guildId = guild.getIdLong();

            GuildUtils.guildMemberCountCache.invalidate(guildId);
            variables.getGuildSettingsCache().invalidate(guildId);
        }
    }

    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        if (event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        final GuildMemberInfo guildCounts = GuildUtils.guildMemberCountCache.getIfPresent(guild.getIdLong());

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

        final GuildSetting settings = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);

        if (settings.isEnableJoinMessage() && settings.getWelcomeLeaveChannel() > 0) {
            final long welcomeLeaveChannelId = settings.getWelcomeLeaveChannel();

            final TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            final String msg = parseGuildVars(settings.getCustomJoinMessage(), event);

            if (!msg.isEmpty() && !"".equals(msg.trim()) && welcomeLeaveChannel != null) {
                sendMsg(welcomeLeaveChannel, msg);
            }
        }

        if (settings.isAutoroleEnabled() && guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            final Role r = guild.getRoleById(settings.getAutoroleRole());

            if (r != null && !guild.getPublicRole().equals(r) && guild.getSelfMember().canInteract(r)) {
                guild.addRoleToMember(event.getMember(), r).queue(null, it -> {});
            }
        }
    }

    private void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        final Guild guild = event.getGuild();
        final User user = event.getUser();
        final SelfUser selfUser = event.getJDA().getSelfUser();

        // If we are leaving we need to ignore this as we cannot send messages to any channels
        // when this event is fired
        if (user.equals(selfUser)) {
            return;
        }

        final GuildMemberInfo guildCounts = GuildUtils.guildMemberCountCache.getIfPresent(guild.getIdLong());

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

        final GuildSetting settings = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);

        // If the leave message is enabled and we have a welcome channel
        if (settings.isEnableJoinMessage() && settings.getWelcomeLeaveChannel() > 0) {
            final long welcomeLeaveChannelId = settings.getWelcomeLeaveChannel();

            final TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            final String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);

            // If we have a message and the text channel is not null
            if (!msg.isEmpty() && !"".equals(msg.trim()) && welcomeLeaveChannel != null) {
                sendMsg(welcomeLeaveChannel, msg);
            }
        }

        if (guild.getIdLong() == Settings.SUPPORT_GUILD_ID) {
            handlePatronRemoval(user.getIdLong());
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
            handlePatronRemoval(event.getUser().getIdLong());
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
                    CommandUtils.guildPatrons.add(userId);
                    typeToSet.set(Patron.Type.ALL_GUILD);
                    return;
                }

                // One guild patron
                if (roleId == Settings.ONE_GUILD_PATRONS_ROLE) {
                    CommandUtils.patrons.remove(userId);
                    handleNewOneGuildPatron(userId);
                    // We assume that the patron already did the steps to register
                    return;
                }

                // Tag patron
                if (roleId == Settings.TAG_PATRONS_ROLE) {
                    CommandUtils.patrons.remove(userId);
                    CommandUtils.tagPatrons.add(userId);
                    typeToSet.set(Patron.Type.TAG);
                    return;
                }

                // Normal patron
                if (roleId == Settings.PATRONS_ROLE) {
                    CommandUtils.patrons.add(userId);
                    typeToSet.set(Patron.Type.NORMAL);
                }
            });

        // if we have a type set it in the database
        // Type is set in the database here to prevent un-needed updates
        if (typeToSet.get() != null) {
            variables.getDatabaseAdapter().createOrUpdatePatron(typeToSet.get(), userId, null);
        }
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
        final GuildSetting s = GuildSettingsUtils.getGuild(guild.getIdLong(), this.variables);
        final long welcomeLeaveChannel = s.getWelcomeLeaveChannel();
        final Parser parser = CommandUtils.PARSER_SUPPLIER.get();

        final String message = parser.put("user", user)
            .put("guild", event.getGuild())
            .put("channel", event.getGuild().getTextChannelById(welcomeLeaveChannel))
            .put("args", "")
            .parse(rawMessage);

        parser.clear();

        return message;
    }

    private void handlePatronRemoval(long userId) {
        // Remove the user from the patrons list
        final boolean hadNormalRank = CommandUtils.patrons.remove(userId);

        // If the main patron role is removed we can just remove the patron from the database
        if (hadNormalRank) {
            variables.getDatabaseAdapter().removePatron(userId);
            return;
        }

        final boolean hadTag = CommandUtils.tagPatrons.remove(userId);
        Patron.Type newType = null;

        if (hadTag) {
            newType = Patron.Type.NORMAL;
        }

        boolean hadOneGuild = false;

        if (CommandUtils.oneGuildPatrons.containsKey(userId)) {
            // Remove the user from the one guild patrons
            CommandUtils.oneGuildPatrons.remove(userId);

            hadOneGuild = true;
        }

        final boolean hadGuildPatron = CommandUtils.guildPatrons.remove(userId);

        if (hadOneGuild || hadGuildPatron) {
            newType = Patron.Type.TAG;
        }

        // Remove when null?
        if (newType != null) {
            final Patron patron = new Patron(newType, userId, null);

            variables.getDatabaseAdapter().createOrUpdatePatron(patron);
            CommandUtils.addPatronsFromData(new AllPatronsData(
                List.of(patron),
                List.of(),
                List.of(),
                List.of()
            ));
        }
    }

    private void handleNewOneGuildPatron(long userId) {
        variables.getDatabaseAdapter().getOneGuildPatron(userId,
            (results) -> {
                results.forEachEntry(
                    (a, guildId) -> {
                        CommandUtils.oneGuildPatrons.put(userId, guildId);

                        return true;
                    }
                );

                return null;
            }
        );
    }
}
