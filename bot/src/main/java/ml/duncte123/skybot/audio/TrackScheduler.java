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

import dev.arbjerg.lavalink.protocol.v4.Exception;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
import dev.arbjerg.lavalink.protocol.v4.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.extensions.AudioTrackKt;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.utils.Debouncer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.SkyBot.getInstance;
import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL;

// TODO: player events
public class TrackScheduler {

    // TODO: keep track of user-data per track (need something unique)
    public static final int MAX_QUEUE_SIZE = 100;
    private final Queue<Track> queue = new LinkedList<>();
    private final Map<String, TrackUserData> userData = new HashMap<>();

    private static final long DEBOUNCE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);
    private final GuildMusicManager guildMusicManager;
    private final Debouncer<String> messageDebouncer;
    private boolean looping = false;
    private boolean loopingQueue = false;

    /* package */ TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
        this.messageDebouncer = new Debouncer<>((msg) -> {
            final MessageChannel latestChannel = guildMusicManager.getLatestChannel();

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

    public Queue<Track> getQueue() {
        return queue;
    }

    public void addToQueue(Track track, boolean isPatron) throws LimitReachedException {
        if (queue.size() + 1 >= MAX_QUEUE_SIZE && !isPatron) {
            throw new LimitReachedException("The queue is full", MAX_QUEUE_SIZE);
        }

        if (this.guildMusicManager.getPlayer().getCurrentTrack() == null) {
            this.play(track);
        } else {
            queue.offer(track);
        }
    }

    /**
     * This is a special case for the skip command where it has to announce the next track
     * due to it being a user interaction
     */
    public void skipCurrentTrack() {
        this.skipTrack(true);
    }

    private void skipTrack(boolean wasFromSkip) {
        skipTracks(1, wasFromSkip);
    }

    public void skipTracks(int count, boolean wasFromSkip) {
        Track nextTrack = null;

        for (int i = 0; i < count; i++) {
            nextTrack = queue.poll();
        }

        if (nextTrack == null) {
            this.guildMusicManager.getPlayer().stopPlayback();
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(guildMusicManager.getLatestChannel())
                    .setMessage("Queue concluded")
            );
        } else {
            // Make sure to cary over the skip state, we want to announce skipped tracks
            getUserData(nextTrack).setWasFromSkip(wasFromSkip);

            this.play(nextTrack);
        }
    }

    public void onTrackStart(Track track) {
        final TrackUserData data = getUserData(track);

        // TODO: do I still need "wasFromSkip" status tracking?
        // If the track was a skipped track, or we announce tracks
        if (data != null && data.getWasFromSkip() || this.guildMusicManager.isAnnounceTracks()) {
            // Reset the was from skip status
            if (data != null) {
                data.setWasFromSkip(false);
            }

            AudioTrackKt.toEmbed(
                track,
                this.guildMusicManager,
                getInstance().getShardManager(),
                false,
                (message) -> {
                    sendMsg(
                        new MessageConfig.Builder()
                            .setChannel(guildMusicManager.getLatestChannel())
                            .setEmbeds(false, message)
                    );

                    return null;
                }
            );
        }
    }

    public void onTrackEnd(Track lastTrack, AudioTrackEndReason endReason) {
        LOGGER.debug("track ended");

        if (!endReason.getMayStartNext()) {
            LOGGER.debug("Cannot start next");
            return;
        }

        // Get if the track was from a skip event
        final TrackUserData userData = getUserData(lastTrack);
        final boolean wasFromSkip = userData.getWasFromSkip();

        if (this.looping) {
            LOGGER.debug("repeating the current song");

            final Track clone = TrackUtilsKt.makeClone(lastTrack);
            storeUserData(clone, createNewTrackData(lastTrack, wasFromSkip));
            this.play(clone);

            return;
        } else if (this.loopingQueue) {
            LOGGER.debug("repeating the queue");
            //Offer it to the queue to prevent the player from playing it
            final Track clone = TrackUtilsKt.makeClone(lastTrack);
            storeUserData(clone, createNewTrackData(lastTrack, wasFromSkip));
            queue.offer(clone);
        }

        // clean up the old track's userdata
        removeUserData(lastTrack);

        LOGGER.debug("can start next track");

        skipTrack(wasFromSkip);
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

    private TrackUserData createNewTrackData(Track track, boolean wasFromSkip) {
        final TrackUserData oldData = getUserData(track);
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

    public void onTrackException(Track track, Exception exception) {
        final String finalCause = Objects.requireNonNullElse(exception.getMessage(), exception.getCause());
        final TrackInfo info = track.getInfo();

        if (finalCause.contains("Something went wrong when decoding the track.")) {
            return;
        }

        if (finalCause.contains("age-restricted")) {
            this.messageDebouncer.accept("Cannot play `" + info.getTitle() + "` because it is age-restricted");
            return;
        }

        this.messageDebouncer.accept("Something went wrong while playing track with identifier `" +
            info.getIdentifier()
            + "`, please contact the devs if this happens a lot.\n" +
            "Details: " + finalCause);
    }

    public TrackUserData getUserData(Track track) {
        final var element = Objects.requireNonNull((JsonPrimitive) track.getUserData().get("uuid"));

        return this.userData.get(element.getContent());
    }

    public void storeUserData(Track track, TrackUserData data) {
        final var element = Objects.requireNonNull((JsonPrimitive) track.getUserData().get("uuid"));

        this.userData.put(element.getContent(), data);
    }

    public void removeUserData(Track track) {
        final var element = Objects.requireNonNull((JsonPrimitive) track.getUserData().get("uuid"));

        this.userData.remove(element.getContent());
    }

    private void play(Track track) {
        this.guildMusicManager.getPlayer()
            .getLink()
            .updatePlayer(
                (builder) -> builder.applyTrack(track)
            )
            .subscribe();
    }
}
