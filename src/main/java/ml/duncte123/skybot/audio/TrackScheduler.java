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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import lavalink.client.player.LavalinkPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.audio.sourcemanagers.spotify.SpotifyAudioTrack;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.extensions.AudioTrackKt;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.utils.Debouncer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.SkyBot.getInstance;
import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL;

public class TrackScheduler extends AudioEventAdapterWrapped {

    public static final int MAX_QUEUE_SIZE = 100;
    public final Queue<AudioTrack> queue;
    private static final long DEBOUNCE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);
    private final LavalinkPlayer player;
    private final GuildMusicManager guildMusicManager;
    private final Debouncer<String> messageDebouncer;
    private boolean looping = false;
    private boolean loopingQueue = false;

    /* package */ TrackScheduler(LavalinkPlayer player, GuildMusicManager guildMusicManager) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.guildMusicManager = guildMusicManager;
        this.messageDebouncer = new Debouncer<>((msg) -> {
            final TextChannel latestChannel = guildMusicManager.getLatestChannel();

            if (latestChannel == null) {
                return;
            }

            sendMsg(new MessageConfig.Builder()
                .setChannel(latestChannel)
                .setMessage(msg)
                .setFailureAction(new ErrorHandler().ignore(UNKNOWN_CHANNEL, MISSING_PERMISSIONS))
                .build());
        }, DEBOUNCE_INTERVAL);
    }

    public boolean canQueue() {
        return this.queue.size() < MAX_QUEUE_SIZE;
    }

    public void addToQueue(AudioTrack track, boolean isPatron) throws LimitReachedException {
        if (queue.size() + 1 >= MAX_QUEUE_SIZE && !isPatron) {
            throw new LimitReachedException("The queue is full", MAX_QUEUE_SIZE);
        }

        if (player.getPlayingTrack() == null) {
            this.play(track);
        } else {
            queue.offer(track);
        }
    }

    /**
     * This is a special case for the skip command where it has to announce the next track
     * due to it being a user interaction
     */
    public void specialSkipCase() {
        // Get the currently playing track
        final AudioTrack playingTrack = this.player.getPlayingTrack();
        // Set in the data that it was from a skip
        playingTrack.getUserData(TrackUserData.class).setWasFromSkip(true);

        // We trigger a fake on track end here to make it adhere to the normal loop flow
        // and inject a boolean for forcing the announcement on skip
        this.onTrackEnd(null, playingTrack, AudioTrackEndReason.FINISHED);
    }

    private void skipTrack() {
        skipTracks(1);
    }

    public void skipTracks(int count) {
        AudioTrack nextTrack = null;

        for (int i = 0; i < count; i++) {
            nextTrack = queue.poll();
        }

        if (nextTrack == null) {
            player.stopTrack();
            sendMsg(guildMusicManager.getLatestChannel(), "Queue concluded");
        } else {
            this.play(nextTrack);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        final TrackUserData data = track.getUserData(TrackUserData.class);

        // If the track was a skipped track, or we announce tracks
        if (data != null && data.getWasFromSkip() || this.guildMusicManager.isAnnounceTracks()) {
            // Reset the was from skip status
            if (data != null) {
                data.setWasFromSkip(false);
            }

            final EmbedBuilder message = AudioTrackKt.toEmbed(
                track,
                this.guildMusicManager,
                getInstance().getShardManager(),
                false
            );

            sendEmbed(this.guildMusicManager.getLatestChannel(), message, false);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack lastTrack, AudioTrackEndReason endReason) {
        LOGGER.debug("track ended");

        if (!endReason.mayStartNext) {
            LOGGER.debug("Cannot start next");
            return;
        }

        // Get if the track was from a skip event
        final boolean wasFromSkip = lastTrack.getUserData(TrackUserData.class).getWasFromSkip();

        if (this.looping) {
            LOGGER.debug("repeating the current song");

            final AudioTrack clone = lastTrack.makeClone();
            clone.setUserData(createNewTrackData(lastTrack, wasFromSkip));
            this.play(clone);

            return;
        } else if (this.loopingQueue) {
            LOGGER.debug("repeating the queue");
            //Offer it to the queue to prevent the player from playing it
            final AudioTrack clone = lastTrack.makeClone();
            clone.setUserData(createNewTrackData(lastTrack, wasFromSkip));
            queue.offer(clone);
        }

        LOGGER.debug("can start next track");

        skipTrack();
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isLoopingQueue() {
        return loopingQueue;
    }

    public void setLoopingQueue(boolean loopingQueue) {
        this.loopingQueue = loopingQueue;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    private TrackUserData createNewTrackData(AudioTrack track, boolean wasFromSkip) {
        final TrackUserData oldData = track.getUserData(TrackUserData.class);
        final TrackUserData newData;

        // If we did not have old data (unlikely) we will create it
        if (oldData == null) {
            newData = new TrackUserData(0L);
        } else {
            newData = oldData.copy(oldData.getRequester());
        }

        // Set the was from skip status on the track
        newData.setWasFromSkip(wasFromSkip);

        return newData;
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        final Throwable rootCause = ExceptionUtils.getRootCause(exception);
        final Throwable finalCause = rootCause == null ? exception : rootCause;
        final AudioTrackInfo info = track.getInfo();

        if (finalCause == null || finalCause.getMessage() == null) {
            this.messageDebouncer.accept("Something went terribly wrong when playing track with identifier `" + info.identifier +
                "`\nPlease contact the developers asap with the identifier in the message above");
            return;
        }

        if (finalCause.getMessage().contains("Something went wrong when decoding the track.")) {
            return;
        }

        if (finalCause.getMessage().contains("age-restricted")) {
            this.messageDebouncer.accept("Cannot play `" + info.title + "` because it is age-restricted");
            return;
        }

        this.messageDebouncer.accept("Something went wrong while playing track with identifier `" +
            info.identifier
            + "`, please contact the devs if this happens a lot.\n" +
            "Details: " + finalCause);

        // old shit
        /*if (exception.severity != FriendlyException.Severity.COMMON) {
            final TextChannel tc = guildMusicManager.getLatestChannel();
            final Guild g = tc == null ? null : tc.getGuild();

            if (g != null) {
                final AudioTrackInfo info = track.getInfo();
                final String error = String.format(
                    "Guild %s (%s) had an FriendlyException on track \"%s\" by \"%s\" (source %s) (%s)",
                    g.getName(),
                    g.getId(),
                    info.title,
                    info.author,
                    track.getSourceManager().getSourceName(),
                    info.identifier
                );

                logger.error(TextColor.RED + error + TextColor.RESET, exception);
            }

        }*/
    }

    public void play(AudioTrack track) {
        // load the youtube identifier before we play the track
        if (track instanceof SpotifyAudioTrack) {
            track.getIdentifier();
        }

        this.player.playTrack(track);
    }
}
