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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

import java.util.List;

public class BigChungusPlaylist extends BasicAudioPlaylist {
    private final int originalSize;

    public BigChungusPlaylist(String name, List<AudioTrack> tracks, AudioTrack selectedTrack, boolean isSearchResult, int originalSize) {
        super(name, tracks, selectedTrack, isSearchResult);

        this.originalSize = originalSize;
    }

    public int getOriginalSize() {
        return originalSize;
    }

    public boolean isBig() {
        return originalSize > getTracks().size();
    }
}
