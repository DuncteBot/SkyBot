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

import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class GuildListener extends BaseListener {

    public GuildListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildJoinEvent) {
            this.onGuildJoin((GuildJoinEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            this.onGuildLeave((GuildLeaveEvent) event);
        } else if (event instanceof GuildVoiceLeaveEvent) {
            this.onGuildVoiceLeave((GuildVoiceLeaveEvent) event);
        } else if (event instanceof GuildVoiceJoinEvent) {
            this.onGuildVoiceJoin((GuildVoiceJoinEvent) event);
        } else if (event instanceof GuildVoiceMoveEvent) {
            this.onGuildVoiceMove((GuildVoiceMoveEvent) event);
        } else if (event instanceof GuildBanEvent) {
            this.onGuildBan((GuildBanEvent) event);
        } else if (event instanceof GuildUnbanEvent) {
            this.onGuildUnban((GuildUnbanEvent) event);
        }
    }

    private void onGuildJoin(GuildJoinEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        logger.info("{}Joining guild {}, ID: {} on shard {}{}",
            TextColor.GREEN,
            guild.getName(),
            guild.getId(),
            guild.getJDA().getShardInfo().getShardId(),
            TextColor.RESET
        );

        GuildSettingsUtils.registerNewGuild(guild, variables);
    }

    private void onGuildLeave(GuildLeaveEvent event) {
        final Guild guild = event.getGuild();
        final GuildMusicManager musicManager = variables.getAudioUtils().getMusicManagers().get(guild.getIdLong());

        if (musicManager != null) {
            if (musicManager.player.getPlayingTrack() != null) musicManager.player.stopTrack();
            musicManager.scheduler.queue.clear();
        }

        logger.info("{}Leaving guild: {} ({}).{}",
            TextColor.RED,
            guild.getName(),
            guild.getId(),
            TextColor.RESET
        );
    }

    private void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        final Guild guild = event.getGuild();
        final LavalinkManager manager = LavalinkManager.ins;

        handleVcAutoRole(guild, event.getMember(), event.getChannelLeft(), true);

        if (!manager.isConnected(guild)) {
            return;
        }

        if (event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        final VoiceChannel vc = manager.getConnectedChannel(guild);

        if (vc == null) {
            return;
        }

        if (!event.getChannelLeft().equals(vc)) {
            return;
        }

        channelCheckThing(guild, event.getChannelLeft());
    }

    private void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();
        final Member self = guild.getSelfMember();
        final VoiceChannel channel = event.getChannelJoined();

        if (member.equals(self)) {
            channelCheckThing(guild, channel);
        }

        handleVcAutoRole(guild, member, channel, false);
    }

    private void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        final Guild guild = event.getGuild();
        final LavalinkManager manager = LavalinkManager.ins;

        if (!manager.isConnected(guild)) {
            return;
        }

        final VoiceChannel connected = manager.getConnectedChannel(guild);

        if (connected == null) {
            return;
        }

        if (event.getChannelJoined().equals(connected) && event.getMember().equals(guild.getSelfMember())) {
            channelCheckThing(guild, connected);

            return;
        }

        if (event.getChannelLeft().equals(connected)) {
            channelCheckThing(guild, event.getChannelLeft());
        }
    }

    private void onGuildUnban(GuildUnbanEvent event) {
        modLogBanUnban(ActionType.UNBAN, event.getUser(), event.getGuild());
    }

    private void onGuildBan(GuildBanEvent event) {
        modLogBanUnban(ActionType.BAN, event.getUser(), event.getGuild());
    }

    private void modLogBanUnban(ActionType type, User user, Guild guild) {
        if (!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
            return;
        }

        final DunctebotGuild dbg = new DunctebotGuild(guild, variables);

        if (dbg.getSettings().getLogChannel() < 1) {
            return;
        }

        guild.retrieveAuditLogs()
            .cache(false)
            .type(type)
            .limit(5)
            .queue((actions) -> {
                for (final AuditLogEntry action : actions) {
                    if (action.getUser() != null && action.getUser().getIdLong() == guild.getSelfMember().getIdLong()) {
                        continue;
                    }

                    if (action.getTargetIdLong() == user.getIdLong()) {
                        ModerationUtils.modLog(
                            action.getUser(),
                            user,
                            type == ActionType.BAN ? "banned" : "unbanned",
                            action.getReason(),
                            dbg
                        );

                        break;
                    }
                }
            });
    }

    private void handleVcAutoRole(Guild guild, Member member, VoiceChannel channel, boolean remove) {
        final Member self = guild.getSelfMember();
        final long guildId = guild.getIdLong();

        final TLongObjectMap<TLongLongMap> vcAutoRoleCache = variables.getVcAutoRoleCache();

        if (!vcAutoRoleCache.containsKey(guildId)) {
            return;
        }

        final TLongLongMap vcToRolePair = vcAutoRoleCache.get(guildId);

        if (vcToRolePair.get(channel.getIdLong()) > 0) {
            final Role role = guild.getRoleById(vcToRolePair.get(channel.getIdLong()));

            if (role != null && self.canInteract(member) && self.canInteract(role) && self.hasPermission(Permission.MANAGE_ROLES)) {
                if (remove) {
                    guild
                        .removeRoleFromMember(member, role)
                        .reason("VC auto role removed")
                        .queue();
                } else {
                    guild
                        .addRoleToMember(member, role)
                        .reason("VC auto role applied")
                        .queue();
                }
            }
        }
    }

    private void channelCheckThing(@Nonnull Guild guild, @Nonnull VoiceChannel voiceChannel) {
        this.handlerThread.submit(() -> {
            try {
                // Run the disconnecting after timeout so we allow JDA to receive updates
                final long timeout = GuildSettingsUtils.getGuild(guild, variables).getLeaveTimeout();
                Thread.sleep(TimeUnit.SECONDS.toMillis(timeout));

                // Make sure to get the vc from JDA because the guild might now update
                final VoiceChannel vc = guild.getJDA().getVoiceChannelById(voiceChannel.getIdLong());

                if (vc == null) {
                    return;
                }

                if (vc.getMembers().stream().anyMatch(m -> !m.getUser().isBot())) {
                    return;
                }

                final GuildMusicManager manager = variables.getAudioUtils().getMusicManagers().get(guild.getIdLong());

                if (manager != null) {
                    manager.player.stopTrack();
                    manager.scheduler.queue.clear();
                }

                MusicCommand.cooldowns.put(guild.getIdLong(), 12600);

                if (LavalinkManager.ins.isConnected(guild)) {
                    LavalinkManager.ins.closeConnection(guild);
                }

                variables.getAudioUtils().getMusicManagers().remove(guild.getIdLong());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
