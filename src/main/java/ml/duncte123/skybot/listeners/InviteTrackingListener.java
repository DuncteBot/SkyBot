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

import com.dunctebot.models.settings.GuildSetting;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.invites.InviteData;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public class InviteTrackingListener extends BaseListener {
    private final Map<String, InviteData> inviteCache = new ConcurrentHashMap<>();

    public InviteTrackingListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (!(event instanceof GenericGuildEvent)) {
            return;
        }

        // events that we always want to handle no mater what (has to do with cleanup)
        // return in both cases so the code stops here
        if (event instanceof GuildInviteDeleteEvent) {
            this.onGuildInviteDelete((GuildInviteDeleteEvent) event);
            return;
        } else if (event instanceof GuildLeaveEvent) {
            this.onGuildLeave((GuildLeaveEvent) event);
            return;
        }

        final Guild guild = ((GenericGuildEvent) event).getGuild();

        if (!isInviteLoggingEnabled(guild)) {
            return;
        }

        if (event instanceof GuildInviteCreateEvent) {
            this.onGuildInviteCreate((GuildInviteCreateEvent) event);
        } else if (event instanceof GuildMemberJoinEvent) {
            this.onGuildMemberJoin((GuildMemberJoinEvent) event);
        } else if (event instanceof GuildReadyEvent) {
            this.onGuildReady((GuildReadyEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            // probably not needed, doubt that any guilds will be patreon guilds on join
            this.onGuildJoin((GuildJoinEvent) event);
        }
    }

    private void onGuildInviteCreate(GuildInviteCreateEvent event) {
        final String code = event.getCode();
        final InviteData inviteData = InviteData.from(event.getInvite());

        inviteCache.put(code, inviteData);
    }

    private void onGuildInviteDelete(GuildInviteDeleteEvent event) {
        final String code = event.getCode();

        inviteCache.remove(code);
    }

    private void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        final User user = event.getUser();
        final Member selfMember = guild.getSelfMember();

        if (!selfMember.hasPermission(Permission.MANAGE_SERVER) || user.isBot()) {
            return;
        }

        if (inviteCache.isEmpty()) {
            // we can't check for invites if they are empty, we'll cache the invites for future use
            attemptInviteCaching(guild);
            return;
        }

        guild.retrieveInvites().queue((invites) -> {
            boolean inviteFound = false;

            for (final Invite invite : invites) {
                // break out of the loop to prevent looping over other invites
                if (inviteFound) {
                    break;
                }

                final String code = invite.getCode();
                final InviteData cachedInvite = inviteCache.get(code);

                if (cachedInvite == null) {
                    continue;
                }

                if (invite.getUses() == cachedInvite.getUses()) {
                    continue;
                }

                inviteFound = true;

                cachedInvite.incrementUses();

                final String pattern = "User **%s** used invite with url <%s>, created by **%s** to join.";
                final String tag = user.getAsTag();
                final String url = invite.getUrl();
                @SuppressWarnings("ConstantConditions") final String inviterTag = invite.getInviter().getAsTag();
                final String toLog = String.format(pattern, tag, url, inviterTag);

                modLog(
                    toLog,
                    new DunctebotGuild(guild, variables)
                );
            }
        });
    }

    private void onGuildReady(final GuildReadyEvent event) {
        final Guild guild = event.getGuild();

        attemptInviteCaching(guild);
    }

    private void onGuildJoin(final GuildJoinEvent event) {
        final Guild guild = event.getGuild();

        attemptInviteCaching(guild);
    }

    private void onGuildLeave(final GuildLeaveEvent event) {
        final long guildId = event.getGuild().getIdLong();

        clearInvites(guildId);
    }

    public void clearInvites(long guildId) {
        inviteCache.entrySet().removeIf(entry -> entry.getValue().getGuildId() == guildId);
    }

    public void attemptInviteCaching(Guild guild) {
        if (!isInviteLoggingEnabled(guild)) {
            return;
        }

        final Member selfMember = guild.getSelfMember();

        if (!selfMember.hasPermission(Permission.MANAGE_SERVER)) {
            return;
        }

        guild.retrieveInvites().queue((invites) ->
            invites.forEach(
                (invite) -> inviteCache.put(invite.getCode(), InviteData.from(invite))
            )
        );
    }

    private boolean isInviteLoggingEnabled(Guild guild) {
        if (!CommandUtils.isGuildPatron(guild)) {
            return false;
        }

        final GuildSetting guildSetting = GuildSettingsUtils.getGuild(guild.getIdLong(), variables);

        return guildSetting.isFilterInvites();
    }
}
