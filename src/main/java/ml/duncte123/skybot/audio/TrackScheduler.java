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
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.utils.Debouncer;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static ml.duncte123.skybot.SkyBot.getInstance;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TrackScheduler extends AudioEventAdapterWrapped {

    public static final int QUEUE_SIZE = 50;
    public final Queue<AudioTrack> queue;
    private static long DEBOUNCE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final IPlayer player;
    private final GuildMusicManager guildMusicManager;
    private final Variables variables;
    private final Debouncer<String> messageDebouncer;
    private boolean repeating = false;
    private boolean repeatPlayList = false;


    /**
     * This instantiates our player
     *
     * @param player
     *         Our audio player
     */
    TrackScheduler(IPlayer player, GuildMusicManager guildMusicManager, Variables variables) {
        this.player = player;
        this.queue = new LinkedList<>();
        this.guildMusicManager = guildMusicManager;
        this.messageDebouncer = new Debouncer<>((msg) ->
            MessageUtils.sendMsg(guildMusicManager.getLatestChannel(), msg, null, (t) -> {})
            , DEBOUNCE_INTERVAL);
        this.variables = variables;
    }

    /**
     * Queue a track
     *
     * @param track
     *         The {@link AudioTrack AudioTrack} to queue
     *
     * @throws LimitReachedException when the queue is full
     */
    public void queue(AudioTrack track) throws LimitReachedException {
        if (queue.size() >= QUEUE_SIZE) {
            throw new LimitReachedException("The queue is full", QUEUE_SIZE);
        }

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
        final AudioTrack nextTrack = queue.poll();

        if (nextTrack != null) {
            player.playTrack(nextTrack);
            announceNextTrack(nextTrack);
            return;
        }

        if (player.getPlayingTrack() != null) {
            player.stopTrack();
        }
    }

    /**
     * Gets run when a track ends
     *
     * @param player
     *         The {@link AudioPlayer AudioTrack} for that guild
     * @param lastTrack
     *         The {@link AudioTrack AudioTrack} that ended
     * @param endReason
     *         Why did this track end?
     */
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack lastTrack, AudioTrackEndReason endReason) {
        logger.debug("track ended");

        if (!endReason.mayStartNext) {
            return;
        }

        logger.debug("can start");

        if (!repeating) {
            logger.debug("starting next track");
            nextTrack();
            return;
        }

        logger.debug("repeating");

        if (repeatPlayList) {
            logger.debug("a playlist.....");
            nextTrack();
            //Offer it to the queue to prevent the player from playing it
            final AudioTrack clone = lastTrack.makeClone();
            clone.setUserData(lastTrack.getUserData());
            queue.offer(clone);
            return;
        }

        final AudioTrack clone = lastTrack.makeClone();
        clone.setUserData(lastTrack.getUserData());
        this.player.playTrack(clone);
        announceNextTrack(lastTrack, true);

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
     * @param repeating
     *         if the player needs to repeat
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
     * @param repeatingPlaylists
     *         if the player needs to repeat playlists
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
        announceNextTrack(track, false);
    }

    private void announceNextTrack(AudioTrack track, boolean repeated) {
        if (guildMusicManager.isAnnounceTracks()) {

            String title = track.getInfo().title;
            title = AudioLoader.getSteamTitle(track, title, variables.getCommandManager());

            final TrackUserData userData = (TrackUserData) track.getUserData();
            final User user = userData == null ?
                getInstance().getShardManager().getShardById(0).getSelfUser() :
                getInstance().getShardManager().getUserById(userData.getUserId());
            final String message = String.format("Now playing: %s %s%nRequester: %#s", title, (repeated ? "(repeated)" : ""), user);

            MessageUtils.sendMsg(guildMusicManager.getLatestChannel(), message, null, (t) -> {});
        }
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
            final Throwable finalCause = rootCause != null ? rootCause : exception;

            if (finalCause.getMessage().contains("Something went wrong when decoding the track.")) {
                return;
            }

            this.messageDebouncer.accept("Something went wrong while playing the track, please contact the devs if this happens a lot.\n" +
                "Details: " + finalCause);
        }
    }
}
