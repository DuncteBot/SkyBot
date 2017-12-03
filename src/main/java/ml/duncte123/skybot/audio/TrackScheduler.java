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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapter {

    /**
     * This stores our queue
     */
    public final Queue<AudioTrack> queue;

    /**
     * Hey look at that, it's our player
     */
    final AudioPlayer player;

    /**
     * This is the last playing track
     */
    AudioTrack lastTrack;

    /**
     * Are we repeating the track
     */
    private boolean repeating = false;

    /**
     * This instantiates our player
     *
     * @param player Our audio player
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    /**
     * Queue a track
     *
     * @param track The {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack AudioTrack} to queue
     */
    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Starts the next track
     */
    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    /**
     * Gets run when a track ends
     *
     * @param player    The {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer AudioTrack} for that guild
     * @param track     The {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack AudioTrack} that ended
     * @param endReason Why did this track end?
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        if (endReason.mayStartNext) {
            if (repeating) {
                player.startTrack(lastTrack.makeClone(), false);
            } else {
                nextTrack();
            }
        }
    }

    /**
     * This will tell you if the player is repeating
     *
     * @return true if the player is set to repeat
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * tell the player if needs to repeat
     *
     * @param repeating if the player needs to repeat
     */
    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    /**
     * Shuffles the player
     */
    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

}
