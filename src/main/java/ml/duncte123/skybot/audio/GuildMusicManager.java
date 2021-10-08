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

package ml.duncte123.skybot.audio;

import fredboat.audio.player.LavalinkManager;
import lavalink.client.player.LavalinkPlayer;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class GuildMusicManager {
    public final LavalinkPlayer player;
    private final TrackScheduler scheduler;
    private final AtomicLong latestChannel = new AtomicLong(-1);
    private final Supplier<Boolean> announceTracksSupplier;

    public GuildMusicManager(long guildId, Variables variables) {
        this.player = LavalinkManager.INS.createPlayer(guildId);
        this.scheduler = new TrackScheduler(this.player, this);
        this.player.addListener(this.scheduler);
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
        final TrackScheduler scheduler = this.getScheduler();
        this.player.removeListener(scheduler);
        this.player.setPaused(false);

        if (this.player.getPlayingTrack() != null) {
            this.player.stopTrack();
        }

        scheduler.queue.clear();
    }

    @Nullable
    public TextChannel getLatestChannel() {
        final long last = this.getLatestChannelId();

        if (last == -1 || last == 0) {
            return null;
        }

        return SkyBot.getInstance().getShardManager().getTextChannelById(last);
    }
}
