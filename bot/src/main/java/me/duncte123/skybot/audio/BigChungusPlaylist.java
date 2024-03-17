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

import dev.arbjerg.lavalink.client.player.Track;

import java.util.List;

public class BigChungusPlaylist {
    private final int originalSize;
    private final String name;
    private final List<Track> tracks;

    public BigChungusPlaylist(String name, List<Track> tracks, int originalSize) {
        this.name = name;
        this.tracks = tracks;
        this.originalSize = originalSize;
    }

    public String getName() {
        return name;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int getOriginalSize() {
        return originalSize;
    }

    public boolean isBig() {
        return originalSize > tracks.size();
    }
}
