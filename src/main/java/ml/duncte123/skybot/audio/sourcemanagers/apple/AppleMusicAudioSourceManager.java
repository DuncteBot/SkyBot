/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.audio.sourcemanagers.apple;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.audio.SupportsPatron;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AppleMusicAudioSourceManager implements AudioSourceManager, SupportsPatron {
    // https://developer.apple.com/documentation/applemusicapi/

    // playlist: https://music.apple.com/nl/playlist/lofi-rap/pl.u-4JomXd3CXxPgX2g?l=en
    // album: https://music.apple.com/nl/album/sparkle-mountain-single/1525954043
    // Artist: https://music.apple.com/nl/artist/andrew-huang/130057628?l=en
    // songs seem to have the i parameter for that
    // Song: https://music.apple.com/nl/album/nintendo-before-school/1445190129?i=1445190410&l=en

    @Override
    public String getSourceName() {
        return "apple_music";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        return loadItem(reference, false);
    }

    @Override
    public AudioItem loadItem(AudioReference reference, boolean isPatron) {
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // No encoding needed
    }

    // TODO
    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {
        // TODO
    }
}
