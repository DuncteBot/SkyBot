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
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import io.sentry.Sentry;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GuildListener extends BaseListener {

    public GuildListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildJoinEvent guildJoin) {
            this.onGuildJoin(guildJoin);
        } else if (event instanceof GuildLeaveEvent guildLeave) {
            this.onGuildLeave(guildLeave);
        } else if (event instanceof GuildVoiceLeaveEvent guildVoiceLeave) {
            this.onGuildVoiceLeave(guildVoiceLeave);
        } else if (event instanceof GuildVoiceJoinEvent guildVoiceJoin) {
            this.onGuildVoiceJoin(guildVoiceJoin);
        } else if (event instanceof GuildVoiceMoveEvent guildVoiceMove) {
            this.onGuildVoiceMove(guildVoiceMove);
        } else if (event instanceof GuildBanEvent guildBan) {
            this.onGuildBan(guildBan);
        } else if (event instanceof GuildUnbanEvent guildUnban) {
            this.onGuildUnban(guildUnban);
        } else if (event instanceof GuildReadyEvent guildReady) {
            this.onGuildReady(guildReady);
        }
    }

    private void onGuildReady(GuildReadyEvent event) {
        if (event.getGuild().getIdLong() == Settings.SUPPORT_GUILD_ID) {
            // Load the members into the member cache
            event.getGuild().loadMembers()
                .onSuccess((unused) -> LOGGER.info("Loaded members for DuncteBot guild"))
                .onError((e) -> LOGGER.error("Failed to load members for DuncteBot guild", e));
        }
    }

    private void onGuildJoin(GuildJoinEvent event) {
        final Guild guild = event.getGuild();

        if (isBotfarm(guild)) {
            return;
        }

        LOGGER.info("{}Joining guild {}, ID: {} on shard {}{}",
            TextColor.GREEN,
            guild.getName(),
            guild.getId(),
            guild.getJDA().getShardInfo().getShardId(),
            TextColor.RESET
        );

        GuildSettingsUtils.registerNewGuild(guild.getIdLong(), variables);
    }

    private void onGuildLeave(GuildLeaveEvent event) {
        final Guild guild = event.getGuild();

        variables.getAudioUtils().removeMusicManager(guild);

        LOGGER.info("{}Leaving guild: {} ({}).{}",
            TextColor.RED,
            guild.getName(),
            guild.getId(),
            TextColor.RESET
        );
    }

    private void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        final Guild guild = event.getGuild();
        final LavalinkManager manager = LavalinkManager.INS;

        handleVcAutoRole(guild, event.getMember(), event.getChannelLeft(), true);

        if (!manager.isConnected(guild)) {
            return;
        }

        if (event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        final VoiceChannel channel = manager.getConnectedChannel(guild);

        if (channel == null) {
            return;
        }

        if (!event.getChannelLeft().equals(channel)) {
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
        final LavalinkManager manager = LavalinkManager.INS;

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
        final GuildSetting settings = dbg.getSettings();

        if (settings.getLogChannel() < 1) {
            return;
        }

        // If unban and unban logging is disabled
        if (type == ActionType.UNBAN && !settings.isUnbanLogging()) {
            return;
        }

        // If ban and ban logging is disabled
        if (type == ActionType.BAN && !settings.isBanLogging()) {
            return;
        }

        guild.retrieveAuditLogs()
            .cache(false)
            .type(type)
            .limit(10)
            .queue((actions) -> {
                final long selfId = guild.getSelfMember().getIdLong();
                final Optional<AuditLogEntry> optionalAction = actions.stream()
                    // ignore any actions that where done by the bot
                    .filter((action) -> action.getUser() != null && action.getUser().getIdLong() != selfId)
                    // Find the action with our banned user
                    .filter((action) -> action.getTargetIdLong() == user.getIdLong())
                    .findFirst();

                if (optionalAction.isPresent()) {
                    final AuditLogEntry action = optionalAction.get();

                    // we filter on null users
                    //noinspection ConstantConditions
                    ModerationUtils.modLog(
                        action.getUser(),
                        user,
                        type == ActionType.BAN ? "banned" : "unbanned",
                        action.getReason(),
                        null,
                        dbg
                    );
                }
            });
    }

    private void handleVcAutoRole(Guild guild, Member member, VoiceChannel channel, boolean remove) {
        final long guildId = guild.getIdLong();
        final TLongObjectMap<TLongLongMap> vcAutoRoleCache = variables.getVcAutoRoleCache();

        if (!vcAutoRoleCache.containsKey(guildId)) {
            return;
        }

        final TLongLongMap vcToRolePair = vcAutoRoleCache.get(guildId);

        if (vcToRolePair.get(channel.getIdLong()) > 0) {
            final Member self = guild.getSelfMember();
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
                final long timeout = GuildSettingsUtils.getGuild(guild.getIdLong(), variables).getLeaveTimeout();
                TimeUnit.SECONDS.sleep(timeout);

                // Make sure to get the vc from JDA because the guild might now update
                final VoiceChannel channel = guild.getJDA().getVoiceChannelById(voiceChannel.getIdLong());

                if (channel == null) {
                    return;
                }

                if (channel.getMembers().stream().anyMatch(m -> !m.getUser().isBot())) {
                    return;
                }

                variables.getAudioUtils().removeMusicManager(guild);

                // Generate the cooldown keys and set the cooldown
                final String cooldownKey = MusicCommand.KEY_GEN.apply(guild.getId());
                final int musicCooldown = MusicCommand.MUSIC_COOLDOWN;

                variables.getCommandManager().setCooldown(cooldownKey, musicCooldown);

                if (LavalinkManager.INS.isConnected(guild)) {
                    LavalinkManager.INS.closeConnection(guild);
                }
            }
            catch (Exception e) {
                Sentry.captureException(e);
            }
        });
    }
}
