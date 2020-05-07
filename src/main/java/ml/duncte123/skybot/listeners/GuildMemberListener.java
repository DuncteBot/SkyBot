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
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

import javax.annotation.Nonnull;

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
        }
    }

    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        if (event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

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

        final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

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

        for (final Role role : event.getRoles()) {
            final long roleId = role.getIdLong();

            if (roleId != Settings.PATRONS_ROLE && roleId != Settings.GUILD_PATRONS_ROLE && roleId != Settings.ONE_GUILD_PATRONS_ROLE) {
                continue;
            }

            handlePatronRemoval(event.getUser().getIdLong());
        }
    }

    private void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

        if (event.getGuild().getIdLong() != Settings.SUPPORT_GUILD_ID) {
            return;
        }

        final User user = event.getUser();
        final long userId = user.getIdLong();

        for (final Role role : event.getRoles()) {
            final long roleId = role.getIdLong();

            if (roleId == Settings.PATRONS_ROLE) {
                CommandUtils.patrons.add(userId);
            }

            if (roleId == Settings.GUILD_PATRONS_ROLE) {
                CommandUtils.guildPatrons.add(userId);
            }

            if (roleId == Settings.ONE_GUILD_PATRONS_ROLE) {
                handleNewOneGuildPatron(userId);
            }
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
        final GuildSettings s = GuildSettingsUtils.getGuild(guild, variables);
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
        boolean removed = CommandUtils.patrons.remove(userId);

        if (CommandUtils.oneGuildPatrons.containsKey(userId)) {
            // Remove the user from the one guild patrons
            CommandUtils.oneGuildPatrons.remove(userId);
//            variables.getDatabaseAdapter().removeOneGuildPatron(userId);

            removed = true;
        }

        removed = removed || CommandUtils.guildPatrons.remove(userId);

        if (removed) {
            variables.getDatabaseAdapter().removePatron(userId);
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
