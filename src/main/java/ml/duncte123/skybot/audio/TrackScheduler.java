/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {

    /**
     * Are we repeating the track
     */
    private boolean repeating = false;
    /**
     * Hey look at that, it's our player
     */
    final AudioPlayer player;
    /**
     * this stores our queue
     */
    public final Queue<AudioTrack> queue;
    /**
     * This is the last playing track
     */
    AudioTrack lastTrack;

    /**
     * This instantiates our player
     * @param player Our audio player
     */
    public TrackScheduler(AudioPlayer player){
        this.player = player;
        this.queue = new LinkedList<>();
    }

    /**
     * Queue a track
     * @param track The {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack AudioTrack} to queue
     */
    public void queue(AudioTrack track){
        if(!player.startTrack(track, true)){
            queue.offer(track);
        }
    }

    /**
     * Starts the next track
     */
    public void nextTrack(){
        player.startTrack(queue.poll(), false);
    }

    /**
     * Gets run when a track ends
     * @param player The {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer AudioTrack} for that guild
     * @param track The {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack AudioTrack} that ended
     * @param endReason Why did this track end?
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason){
        this.lastTrack = track;

        if(endReason.mayStartNext){
            if(repeating){
                player.startTrack(lastTrack.makeClone(), false);
            }else{
                nextTrack();
            }
        }
    }

    /**
     * This will tell you if the player is repeating
     * @return true if the player is set to repeat
     */
    public boolean isRepeating(){
        return repeating;
    }

    /**
     * tell the player if needs to repeat
     * @param repeating if the player needs to repeat
     */
    public void setRepeating(boolean repeating){
        this.repeating = repeating;
    }

    /**
     * Shuffles the player
     */
    public void shuffle(){
        Collections.shuffle((List<?>) queue);
    }

}
