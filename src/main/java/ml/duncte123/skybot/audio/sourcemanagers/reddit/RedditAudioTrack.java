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

package ml.duncte123.skybot.audio.sourcemanagers.reddit;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class RedditAudioTrack extends MpegTrack {
    public RedditAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    protected String getPlaybackUrl() {
        return getPlaybackUrl(this.trackInfo.identifier);
    }

    static String getPlaybackUrl(String id) {
        return "https://v.redd.it/" + id + "/audio?source=fallback";
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new RedditAudioTrack(this.trackInfo, this.getSourceManager());
    }
}
