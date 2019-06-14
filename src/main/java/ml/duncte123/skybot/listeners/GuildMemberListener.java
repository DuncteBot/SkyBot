/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.CustomCommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.member.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class GuildMemberListener extends BaseListener {

    public GuildMemberListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
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
                guild.getController()
                    .addSingleRoleToMember(event.getMember(), r).queue(null, it -> {
                });
            }
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        final Guild guild = event.getGuild();

        if (event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        final GuildSettings settings = GuildSettingsUtils.getGuild(guild, variables);

        if (settings.isEnableJoinMessage() && settings.getWelcomeLeaveChannel() > 0) {
            final long welcomeLeaveChannelId = settings.getWelcomeLeaveChannel();

            final TextChannel welcomeLeaveChannel = guild.getTextChannelById(welcomeLeaveChannelId);
            final String msg = parseGuildVars(settings.getCustomLeaveMessage(), event);

            if (!msg.isEmpty() && !"".equals(msg.trim()) && welcomeLeaveChannel != null) {
                sendMsg(welcomeLeaveChannel, msg);
            }
        }

        if (guild.getIdLong() == Command.supportGuildId) {
            handlePatronRemoval(event.getUser().getIdLong(), event.getJDA().asBot().getShardManager());
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {

        if (event.getGuild().getIdLong() != Command.supportGuildId) {
            return;
        }

        for (final Role role : event.getRoles()) {
            final long roleId = role.getIdLong();

            if (roleId != Command.patronsRole && roleId != Command.guildPatronsRole && roleId != Command.oneGuildPatronsRole) {
                continue;
            }

            handlePatronRemoval(event.getUser().getIdLong(), event.getJDA().asBot().getShardManager());
        }
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

        if (event.getGuild().getIdLong() != Command.supportGuildId) {
            return;
        }

        final User user = event.getUser();
        final long userId = user.getIdLong();
        final ShardManager manager = event.getJDA().asBot().getShardManager();

        for (final Role role : event.getRoles()) {
            final long roleId = role.getIdLong();

            if (roleId == Command.patronsRole) {
                Command.patrons.add(userId);
            }

            if (roleId == Command.guildPatronsRole) {
                final List<Long> guilds = manager.getMutualGuilds(user).stream()
                    .filter((it) -> {
                        Member member = it.getMember(user);

                        return it.getOwner().equals(member) || member.hasPermission(Permission.ADMINISTRATOR);
                    })
                    .map(Guild::getIdLong)
                    .collect(Collectors.toList());

                Command.guildPatrons.addAll(guilds);
            }

            if (roleId == Command.oneGuildPatronsRole) {
                handleNewOneGuildPatron(userId);
            }
        }

    }

    @Nonnull
    private String parseGuildVars(String rawMessage, GenericGuildMemberEvent event) {

        if (!(event instanceof GuildMemberJoinEvent) && !(event instanceof GuildMemberLeaveEvent)) {
            return "NOPE";
        }

        if (rawMessage == null || "".equals(rawMessage.trim().toLowerCase())) {
            return "";
        }

        final Guild guild = event.getGuild();
        final GuildSettings s = GuildSettingsUtils.getGuild(guild, variables);
        final long welcomeLeaveChannel = s.getWelcomeLeaveChannel();
        final Parser parser = CustomCommandUtils.PARSER_SUPPLIER.get();

        final String message = parser.put("user", event.getUser())
            .put("guild", event.getGuild())
            .put("channel", event.getGuild().getTextChannelById(welcomeLeaveChannel))
            .put("args", "")
            .parse(rawMessage);

        parser.clear();

        return message;
    }

    private void handlePatronRemoval(long userId, ShardManager manager) {
        // Remove the user from the patrons list
        Command.patrons.remove(userId);

        if (Command.oneGuildPatrons.containsKey(userId)) {
            // Remove the user from the one guild patrons
            Command.oneGuildPatrons.remove(userId);
            GuildUtils.removeOneGuildPatron(userId, variables.getDatabaseAdapter());
        }

        final User user = manager.getUserById(userId);

        if (user != null) {
            manager.getMutualGuilds(user).forEach(
                (guild) -> Command.guildPatrons.remove(guild.getIdLong())
            );
        }
    }

    private void handleNewOneGuildPatron(long userId) {
        variables.getDatabaseAdapter().getOneGuildPatron(userId,
            (results) -> {
                results.forEachEntry(
                    (a, guildId) -> {
                        Command.oneGuildPatrons.put(userId, guildId);

                        return true;
                    }
                );

                return null;
            }
        );
    }
}
