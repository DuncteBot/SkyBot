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

package me.duncte123.skybot.audio;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.Link;
import fredboat.audio.player.LavalinkManager;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class GuildMusicManager {
    private final long guildId;
    private final TrackScheduler scheduler;
    private final AtomicLong latestChannel = new AtomicLong(-1);
    private final Supplier<Boolean> announceTracksSupplier;

    public GuildMusicManager(long guildId, Variables variables) {
        this.guildId = guildId;
        this.scheduler = new TrackScheduler(this);
        this.announceTracksSupplier = () -> GuildSettingsUtils.getGuild(guildId, variables).isAnnounceTracks();
    }

    /* package */ boolean isAnnounceTracks() {
        return this.announceTracksSupplier.get();
    }

    // Has to be public because of kotlin
    public long getLatestChannelId() {
        return this.latestChannel.get();
    }

    public void setLatestChannelId(long latestChannel) {
        this.latestChannel.set(latestChannel);
    }

    public TrackScheduler getScheduler() {
        return this.scheduler;
    }

    public void stopAndClear() {
        this.getPlayer().ifPresent(
            (player) -> player.setPaused(false)
                .setTrack(null)
                .subscribe()
        );

        this.scheduler.getQueue().clear();
    }

    @Nonnull
    public Optional<MessageChannel> getLatestChannel() {
        final long last = this.getLatestChannelId();

        if (last == -1 || last == 0) {
            return Optional.empty();
        }

        return Optional.ofNullable(
            SkyBot.getInstance().getShardManager().getChannelById(MessageChannel.class, last)
        );
    }

    @Nullable
    public Link getLink() {
        return LavalinkManager.INS.getLavalink().getLinkIfCached(this.guildId);
    }

    public Optional<LavalinkPlayer> getPlayer() {
        final var link = this.getLink();

        if (link == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(link.getCachedPlayer());
    }

    public long getGuildId() {
        return guildId;
    }
}
