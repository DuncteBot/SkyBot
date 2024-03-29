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

package fredboat.audio.player;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LinkState;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.objects.config.DunctebotConfig;
import me.duncte123.skybot.utils.AirUtils;
import me.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class has been taken from
 * https://github.com/Frederikam/FredBoat/blob/master/FredBoat/src/main/java/fredboat/audio/player/LavalinkManager.java
 * and has been modified to fit my needs
 */
public final class LavalinkManager {
    private static final int AUDIO_SESSION_INVALID = 4006;

    private static final Logger LOG = LoggerFactory.getLogger(LavalinkManager.class);

    public static final LavalinkManager INS = new LavalinkManager();
    private LavalinkClient lavalink = null;
    private DunctebotConfig config = null;
    private AudioUtils audioUtils;
    private boolean enabledOverride = true;

    private LavalinkManager() {
    }

    public void start(DunctebotConfig config, AudioUtils audioUtils) {
        this.config = config;
        this.audioUtils = audioUtils;

        if (!isEnabled()) {
            return;
        }

        final long userId = Helpers.getUserIdFromToken(this.config.discord.token);

        lavalink = new LavalinkClient(userId);

        this.registerTrackStartEvent();
        this.registerTrackEndEvent();
        this.registerTrackExceptionEvent();
        this.registerTrackStuckEvent();
        this.registerWebsocketClosedEvent();

        loadNodes();
    }

    @SuppressWarnings("unused") // we need it from eval
    public void forceEnable(boolean enabled) {
        if (enabled) {
            this.start(this.config, this.audioUtils);
        } else {
            AirUtils.stopMusic(this.audioUtils);

            this.lavalink.close();
        }

        // Do this last, otherwise we can't disconnect
        this.enabledOverride = enabled;
    }

    public boolean isEnabled() {
        return this.enabledOverride && config.lavalink.enable;
    }

    public void openConnection(AudioChannel channel) {
        if (isEnabled()) {
            final AudioManager audioManager = channel.getGuild().getAudioManager();

            // Turn on the deafen icon for the bot
            audioManager.setSelfDeafened(true);

            channel.getJDA().getDirectAudioController().connect(channel);
        }
    }

    public void closeConnection(Guild guild) {
        if (isEnabled()) {
            guild.getJDA().getDirectAudioController().disconnect(guild);
        }
    }

    public boolean isConnected(Guild guild) {
        return isConnected(guild.getIdLong());
    }

    public boolean isConnected(long guildId) {
        if (!isEnabled()) {
            return false;
        }

        final var link = lavalink.getLinkIfCached(guildId);

        return link != null && link.getState() == LinkState.CONNECTED;
    }

    @Nullable
    @SuppressWarnings("ConstantConditions") // cache is enabled
    public AudioChannelUnion getConnectedChannel(@Nonnull Guild guild) {
        // NOTE: never use the local audio manager, since the audio connection may be remote
        // there is also no reason to look the channel up remotely from lavalink, if we have access to a real guild
        // object here, since we can use the voice state of ourselves (and lavalink 1.x is buggy in keeping up with the
        // current voice channel if the bot is moved around in the client)
        return guild.getSelfMember().getVoiceState().getChannel();
    }

    public void shutdown() {
        if (isEnabled()) {
            this.lavalink.close();
        }
    }

    public LavalinkClient getLavalink() {
        return lavalink;
    }

    private void loadNodes() {
        final LavalinkClient lavalink = getLavalink();

        for (final DunctebotConfig.Lavalink.LavalinkNode node : config.lavalink.nodes) {
            // TODO: region filter mapping
            lavalink.addNode(
                new NodeOptions.Builder()
                    .setName(node.name)
                    .setServerUri(node.wsurl)
                    .setPassword(node.pass)
                    .build()
            );
        }
    }

    private void registerTrackStartEvent() {
        lavalink.on(TrackStartEvent.class).subscribe((event) -> {
            final long guildIdLong = event.getGuildId();
            final var mng = audioUtils.getMusicManagers().get(guildIdLong);

            if (mng != null) {
                mng.getScheduler().onTrackStart(event.getTrack());
            }
        });
    }

    private void registerTrackEndEvent() {
        lavalink.on(TrackEndEvent.class).subscribe((event) -> {
            final long guildIdLong = event.getGuildId();
            final var mng = audioUtils.getMusicManagers().get(guildIdLong);

            if (mng != null) {
                mng.getScheduler().onTrackEnd(event.getTrack(), event.getEndReason());
            }
        });
    }

    private void registerTrackExceptionEvent() {
        lavalink.on(TrackExceptionEvent.class).subscribe((event) -> {
            final long guildIdLong = event.getGuildId();
            final var mng = audioUtils.getMusicManagers().get(guildIdLong);

            if (mng != null) {
                mng.getScheduler().onTrackException(event.getTrack(), event.getException());
            }
        });
    }

    private void registerTrackStuckEvent() {}

    private void registerWebsocketClosedEvent() {
        lavalink.on(WebSocketClosedEvent.class).subscribe((event) -> {
            if (event.getCode() == AUDIO_SESSION_INVALID) {
                final long guildIdLong = event.getGuildId();
                final var guild = SkyBot.getInstance().getShardManager().getGuildById(guildIdLong);

                if (guild == null) {
                    LOG.debug("Not reconnecting invalid session for {}, getGuildById returned null", guildIdLong);
                    return;
                }

                final var connectedChannel = getConnectedChannel(guild);

                if (connectedChannel == null) {
                    LOG.debug("Not reconnecting invalid session for {}, we're not connected to any channels", guildIdLong);
                    return;
                }

                guild.getJDA().getDirectAudioController().reconnect(connectedChannel);
            }
        });
    }
}
