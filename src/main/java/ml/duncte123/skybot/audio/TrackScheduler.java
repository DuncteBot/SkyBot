/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import ml.duncte123.skybot.commands.music.RadioCommand;
import ml.duncte123.skybot.objects.RadioStream;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TrackScheduler extends AudioEventAdapterWrapped {

    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    public final Queue<AudioTrack> queue;
    private final IPlayer player;
    private final GuildMusicManager guildMusicManager;
    private boolean repeating = false;
    private boolean repeatPlayList = false;


    /**
     * This instantiates our player
     *
     * @param player Our audio player
     */
    TrackScheduler(IPlayer player, GuildMusicManager guildMusicManager) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.guildMusicManager = guildMusicManager;
    }

    /**
     * Queue a track
     *
     * @param track The {@link AudioTrack AudioTrack} to queue
     */
    public void queue(AudioTrack track) {
        if (player.getPlayingTrack() != null) {
            queue.offer(track);
        } else {
            player.playTrack(track);
        }
    }

    /**
     * Starts the next track
     */
    public void nextTrack() {
        if (queue.peek() != null) {
            AudioTrack nextTrack = queue.poll();
            if (nextTrack != null) {
                player.playTrack(nextTrack);
                announceNextTrack(nextTrack);
            }
        } else if (player.getPlayingTrack() != null)
            player.stopTrack();
    }

    /**
     * Gets run when a track ends
     *
     * @param player    The {@link AudioPlayer AudioTrack} for that guild
     * @param track     The {@link AudioTrack AudioTrack} that ended
     * @param endReason Why did this track end?
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        @SuppressWarnings("UnnecessaryLocalVariable") AudioTrack lastTrack = track;
        logger.debug("track ended");
        if (endReason.mayStartNext) {
            logger.debug("can start");
            if (repeating) {
                logger.debug("repeating");
                if (!repeatPlayList) {
                    this.player.playTrack(lastTrack.makeClone());
                    announceNextTrack(lastTrack);
                } else {
                    logger.debug("a playlist.....");
                    nextTrack();
                    //Offer it to the queue to prevent the player from playing it
                    queue.offer(lastTrack.makeClone());
                }
            } else {
                logger.debug("starting next track");
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
     * This will tell you if the player is repeating playlists
     *
     * @return true if the player is set to repeat playlists
     */
    public boolean isRepeatingPlaylists() {
        return repeatPlayList;
    }

    /**
     * tell the player if needs to repeat playlists
     *
     * @param repeatingPlaylists if the player needs to repeat playlists
     */
    public void setRepeatingPlaylists(boolean repeatingPlaylists) {
        this.repeatPlayList = repeatingPlaylists;
    }

    /**
     * Shuffles the player
     */
    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    private void announceNextTrack(AudioTrack track) {
        if (guildMusicManager.guildSettings.isAnnounceTracks()) {
            String title = track.getInfo().title;
            if (track.getInfo().isStream) {
                Optional<RadioStream> stream = ((RadioCommand) AirUtils.COMMAND_MANAGER.getCommand("radio"))
                        .getRadioStreams().stream().filter(s -> s.getUrl().equals(track.getInfo().uri)).findFirst();
                if (stream.isPresent())
                    title = stream.get().getName();
            }
            MessageUtils.sendMsg(guildMusicManager.latestChannel, "Now playing: " + title);
        }
    }

}
