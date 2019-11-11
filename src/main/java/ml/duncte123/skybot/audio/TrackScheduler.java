/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lavalink.client.player.IPlayer;
import lavalink.client.player.event.AudioEventAdapterWrapped;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.extensions.AudioTrackKt;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.utils.Debouncer;
import net.dv8tion.jda.api.EmbedBuilder;
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

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TrackScheduler extends AudioEventAdapterWrapped {

    public static final int QUEUE_SIZE = 50;
    public final Queue<AudioTrack> queue;
    private static long DEBOUNCE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final IPlayer player;
    private final GuildMusicManager guildMusicManager;
    private final Debouncer<String> messageDebouncer;
    private boolean repeating = false;
    private boolean repeatPlayList = false;

    TrackScheduler(IPlayer player, GuildMusicManager guildMusicManager) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.guildMusicManager = guildMusicManager;
        this.messageDebouncer = new Debouncer<>((msg) ->
            sendMsg(guildMusicManager.getLatestChannel(), msg, null, (t) -> {})
            , DEBOUNCE_INTERVAL);
    }

    public void queue(AudioTrack track, boolean isPatron) throws LimitReachedException {
        if (queue.size() >= QUEUE_SIZE && !isPatron) {
            throw new LimitReachedException("The queue is full", QUEUE_SIZE);
        }

        if (player.getPlayingTrack() != null) {
            queue.offer(track);
        } else {
            player.playTrack(track);
        }
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
        } else  {
            player.playTrack(nextTrack);
            announceNextTrack(nextTrack);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack lastTrack, AudioTrackEndReason endReason) {
        logger.debug("track ended");

        if (!endReason.mayStartNext) {
            return;
        }

        logger.debug("can start");

        if (!repeating) {
            logger.debug("starting next track");
            skipTrack();
            return;
        }

        logger.debug("repeating");

        if (repeatPlayList) {
            logger.debug("a playlist.....");
            skipTrack();
            //Offer it to the queue to prevent the player from playing it
            final AudioTrack clone = lastTrack.makeClone();
            clone.setUserData(createNewTrackData(lastTrack));
            queue.offer(clone);
            return;
        }

        final AudioTrack clone = lastTrack.makeClone();
        clone.setUserData(createNewTrackData(lastTrack));
        this.player.playTrack(clone);
        announceNextTrack(lastTrack);
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public boolean isRepeatingPlaylists() {
        return repeatPlayList;
    }

    public void setRepeatingPlaylists(boolean repeatingPlaylists) {
        this.repeatPlayList = repeatingPlaylists;
    }

    public void shuffle() {
        Collections.shuffle((List<?>) queue);
    }

    private void announceNextTrack(AudioTrack track) {
        if (guildMusicManager.isAnnounceTracks()) {
            final EmbedBuilder message = AudioTrackKt.toEmbed(
                track,
                this.guildMusicManager,
                getInstance().getShardManager(),
                false
            );

            sendEmbed(guildMusicManager.getLatestChannel(), message);
        }
    }

    private TrackUserData createNewTrackData(AudioTrack track) {
        final TrackUserData oldData = track.getUserData(TrackUserData.class);

        if (oldData == null) {
            return new TrackUserData(0L);
        }

        return oldData.copy(oldData.getRequester());
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        if (exception.severity != FriendlyException.Severity.COMMON) {
            /*final TextChannel tc = guildMusicManager.getLatestChannel();
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
            }*/

            final Throwable rootCause = ExceptionUtils.getRootCause(exception);
            final Throwable finalCause = rootCause == null ? exception : rootCause;

            if (finalCause.getMessage().contains("Something went wrong when decoding the track.")) {
                return;
            }

            this.messageDebouncer.accept("Something went wrong while playing the track, please contact the devs if this happens a lot.\n" +
                "Details: " + finalCause);
        }
    }
}
