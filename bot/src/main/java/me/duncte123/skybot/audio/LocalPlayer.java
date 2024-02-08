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

import dev.arbjerg.lavalink.client.IUpdatablePlayer;
import dev.arbjerg.lavalink.client.LavalinkPlayer;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.protocol.Track;
import dev.arbjerg.lavalink.protocol.v4.Filters;
import fredboat.audio.player.LavalinkManager;

import javax.annotation.Nullable;
import java.util.Optional;

public class LocalPlayer {
    private final long guildId;

    public LocalPlayer(long guildId) {
        this.guildId = guildId;
    }

    public Link getLink() {
        return LavalinkManager.INS.getLavalink().getLink(this.guildId);
    }

    public IUpdatablePlayer update() {
        return this.getLink().createOrUpdatePlayer();
    }

    public Optional<LavalinkPlayer> getLavalinkPlayer() {
        return Optional.ofNullable(
            this.getLink().getCachedPlayer()
        );
    }

    public void stopPlayback() {
        this.update()
            .setPaused(false)
            .setEncodedTrack(null)
            .subscribe();
    }

    public void setFilters(Filters filters) {
        this.update()
            .setFilters(filters)
            .subscribe();
    }

    public void seekTo(long position) {
        this.update()
            .setPosition(position)
            .subscribe();
    }

    @Nullable
    public Track getCurrentTrack() {
        final LavalinkPlayer player = this.getLink().getCachedPlayer();

        if (player == null) {
            return null;
        }

        return player.getTrack();
    }

    public long getPosition() {
        final LavalinkPlayer player = this.getLink().getCachedPlayer();

        if (player == null) {
            return -1;
        }

        return player.getPosition();
    }
}
