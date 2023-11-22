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

import dev.arbjerg.lavalink.protocol.v4.PlayerState;
import dev.arbjerg.lavalink.protocol.v4.Track;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

public class LocalPlayer {
    private final AtomicReference<Track> currentTrack = new AtomicReference<>(null);
    private final AtomicReference<PlayerState> state = new AtomicReference<>(null);

    @Nullable
    public Track getCurrentTrack() {
        return this.currentTrack.get();
    }

    public long getPosition() {
        final var currentState = this.state.get();

        if (currentState == null) {
            return 0;
        }

        return currentState.getPosition();
    }

    public void updatePlayerState(PlayerState state) {
        this.state.set(state);
    }
}
