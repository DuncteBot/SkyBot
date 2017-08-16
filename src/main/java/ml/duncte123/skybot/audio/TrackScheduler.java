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
	
	private boolean repeating = false;
	final AudioPlayer player;
	public final Queue<AudioTrack> queue;
	AudioTrack lastTrack;

	public TrackScheduler(AudioPlayer player){
		this.player = player;
		this.queue = new LinkedList<AudioTrack>();
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
