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

package ml.duncte123.skybot.audio.sourcemanagers.ocremix;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.Mp3Track;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class OCRemixAudioTrack extends Mp3Track {
    private static final String[] MUSIC_HOSTS = {
        "iterations.org",
        "ocrmirror.org",
        "ocr.blueblue.fr",
    };
    private int hostIndex = 0;

    public OCRemixAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        // attempt to load all hosts if one fails
        try (HttpInterface httpInterface = this.getSourceManager().getHttpInterface()) {
            while (this.hostIndex < MUSIC_HOSTS.length) {
                try {
                    loadStream(executor, httpInterface);
                    break;
                } catch (Exception e) {
                    this.hostIndex++;

                    if ((this.hostIndex >= MUSIC_HOSTS.length)) {
                        throw e;
                    }
                }
            }
        }
    }

    @Override
    protected String getPlaybackUrl() {
        return "https://" + MUSIC_HOSTS[this.hostIndex] + this.trackInfo.uri;
    }
}
