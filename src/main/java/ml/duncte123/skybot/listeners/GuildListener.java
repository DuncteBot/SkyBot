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

package ml.duncte123.skybot.listeners;

import fredboat.audio.player.LavalinkManager;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class GuildListener extends BaseListener {

    public GuildListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
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

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        final Guild guild = event.getGuild();

        logger.info("{}Leaving guild: {} ({}).{}",
            TextColor.RED,
            guild.getName(),
            guild.getId(),
            TextColor.RESET
        );
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        final Guild guild = event.getGuild();
        final LavalinkManager manager = LavalinkManager.ins;

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

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        final Guild guild = event.getGuild();

        if (!event.getMember().equals(guild.getSelfMember())) {
            return;
        }

        channelCheckThing(guild, event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        final Guild guild = event.getGuild();
        final LavalinkManager manager = LavalinkManager.ins;

        if (!manager.isConnected(guild)) {
            return;
        }

        final VoiceChannel connected = manager.getConnectedChannel(guild);

        if (connected == null) {
            return;
        }

        if (!event.getChannelJoined().equals(connected) && event.getMember().equals(guild.getSelfMember())) {
            channelCheckThing(guild, connected);

            return;
        }

        if (event.getChannelLeft().equals(connected)) {
            channelCheckThing(guild, event.getChannelLeft());
        }
    }

    /**
     * This handles the guild leave/ join events to deferments if the channel is empty
     *
     * @param guild
     *         the guild
     * @param vc
     *         the voice channel
     */
    private void channelCheckThing(@NotNull Guild guild, @NotNull VoiceChannel vc) {

        if (vc.getMembers().stream().anyMatch(m -> !m.getUser().isBot())) {
            return;
        }

        final GuildMusicManager manager = variables.getAudioUtils().getMusicManager(guild, false);

        if (manager != null) {
            manager.player.stopTrack();
            manager.player.setPaused(false);
            manager.scheduler.queue.clear();

            sendMsg(guild.getTextChannelById(manager.latestChannel), "Leaving voice channel because all the members have left it.");
        }

        if (guild.getAudioManager().getConnectionListener() != null) {
            guild.getAudioManager().setConnectionListener(null);
        }

        MusicCommand.cooldowns.put(guild.getIdLong(), 12600);

        variables.getDatabase().run(() -> {
            try {
                // Run the disconnecting after 500ms so we allow JDA to receive updates
                Thread.sleep(500L);

                if (LavalinkManager.ins.isConnected(guild)) {
                    LavalinkManager.ins.closeConnection(guild);
                    variables.getAudioUtils().getMusicManagers().remove(guild.getIdLong());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
