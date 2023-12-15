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

import dev.arbjerg.lavalink.client.LavalinkPlayer;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.protocol.v4.Filters;
import dev.arbjerg.lavalink.protocol.v4.PlayerState;
import dev.arbjerg.lavalink.client.protocol.Track;
import fredboat.audio.player.LavalinkManager;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class LocalPlayer {
    private final long guildId;
    private final AtomicReference<Track> currentTrack = new AtomicReference<>(null);
    private final AtomicReference<PlayerState> state = new AtomicReference<>(null);

    public LocalPlayer(long guildId) {
        this.guildId = guildId;
    }

    public Link getLink() {
        return LavalinkManager.INS.getLavalink().getLink(this.guildId);
    }

    public Mono<LavalinkPlayer> getLavalinkPlayer() {
        return this.getLink().getPlayer();
    }

    public void stopPlayback() {
        this.getLink()
            .updatePlayer(
                (player) -> player.setPaused(false)
                    .setEncodedTrack(null)
            )
            .subscribe();
    }

    public void setFilters(Filters filters) {
        this.getLink()
            .updatePlayer(
                (player) -> player.setFilters(filters)
            )
            .subscribe();
    }

    public void seekTo(long position) {
        this.getLink()
           .updatePlayer(
                (player) -> player.setPosition(position)
            )
           .subscribe();
    }

    @Nullable
    public Track getCurrentTrack() {
        return this.currentTrack.get();
    }

    public long getPosition() {
        final var currentState = this.state.get();

        if (currentState == null) {
            final Track currentTrack = getCurrentTrack();

            if (currentTrack == null) {
                return 0;
            }

            return currentTrack.getInfo().getPosition();
        }

        return currentState.getPosition();
    }

    public void updateLocalPlayerState(PlayerState state) {
        this.state.set(state);
    }

    public void updateCurrentTrack(Track track) {
        this.currentTrack.set(track);
    }
}
