/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.audiomanagers.speech;

import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import ml.duncte123.skybot.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class SpeechAudioTrack extends DelegatedAudioTrack {

    private static final Logger log = LoggerFactory.getLogger(SpeechAudioTrack.class);

    private final SpeechAudioSourceManager manager;

    SpeechAudioTrack(AudioTrackInfo trackInfo, SpeechAudioSourceManager manager) {
        super(trackInfo);
        this.manager = manager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (HttpInterface httpInterface = manager.getHttpInterface()) {
            loadStream(executor, httpInterface);
        }
    }

    private void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        String trackUrl = trackInfo.identifier;
        log.debug("Starting Speech speech from URL: {}", trackUrl);

        try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackUrl), null)) {
            processDelegate(new Mp3AudioTrack(trackInfo, stream), localExecutor);
        }
    }

    @Override
    public AudioTrack makeClone() {
        return new SpeechAudioTrack(trackInfo, manager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return manager;
    }
}
