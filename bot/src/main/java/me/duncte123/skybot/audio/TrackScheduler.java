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

package me.duncte123.skybot.audio;

import dev.arbjerg.lavalink.client.protocol.Track;
import dev.arbjerg.lavalink.client.protocol.TrackException;
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.skybot.exceptions.LimitReachedException;
import me.duncte123.skybot.extensions.AudioTrackKt;
import me.duncte123.skybot.objects.TrackUserData;
import me.duncte123.skybot.utils.Debouncer;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.skybot.SkyBot.getInstance;
import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_CHANNEL;

public class TrackScheduler {
    public static final int MAX_QUEUE_SIZE = 100;
    private final Queue<Track> queue = new LinkedList<>();
    private final Map<String, TrackUserData> userData = new ConcurrentHashMap<>();

    private static final long DEBOUNCE_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackScheduler.class);
    private final GuildMusicManager guildMusicManager;
    private final Debouncer<String> messageDebouncer;
    private boolean looping = false;
    private boolean loopingQueue = false;

    /* package */ TrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
        this.messageDebouncer = new Debouncer<>((msg) -> {
            guildMusicManager.getLatestChannel()
                .ifPresent((latestChannel) ->  sendMsg(new MessageConfig.Builder()
                    .setChannel(latestChannel)
                    .setMessage(msg)
                    .setFailureAction(new ErrorHandler().ignore(UNKNOWN_CHANNEL, MISSING_PERMISSIONS))
                    .build()));
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
     *
     * @param tracks The tracks to be added to the queue.
     * @param isPatron if this user is a supporter of the bot.
     * @return The total amount of tracks that were added to the queue.
     * @throws LimitReachedException When the free limit has been reached.
     */
    public int queuePlaylistTracks(List<Track> tracks, boolean isPatron) throws LimitReachedException {
        final List<Track> tmpQueue = new ArrayList<>();

        while (!tracks.isEmpty() && (tmpQueue.size() + queue.size() + 1 < MAX_QUEUE_SIZE || isPatron)) {
            tmpQueue.add(tracks.removeFirst());
        }

        final int tracksAdded = tmpQueue.size();

        if (this.guildMusicManager.getPlayer().getCurrentTrack() == null) {
            this.play(tmpQueue.removeFirst());
        }

        queue.addAll(tmpQueue);

        return tracksAdded;
    }

    /**
     * This is a special case for the skip command where it has to announce the next track
     * due to it being a user interaction
     */
    public void skipCurrentTrack() {
        this.skipTrack(true);
    }

    private void skipTrack(boolean forceAnnounce) {
        skipTracks(1, forceAnnounce);
    }

    public void skipTracks(int count, boolean forceAnnounce) {
        Track nextTrack = null;

        for (int i = 0; i < count; i++) {
            nextTrack = queue.poll();
        }

        if (nextTrack == null) {
            this.guildMusicManager.getPlayer().stopPlayback();

            guildMusicManager.getLatestChannel()
                .ifPresent((channel) -> sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(channel)
                        .setMessage("Queue concluded")
                ));
        } else {
            // Make sure to cary over the skip state, we want to announce skipped tracks
            getUserData(nextTrack).setForceAnnounce(forceAnnounce);

            this.play(nextTrack);
        }
    }

    public void onTrackStart(Track track) {
        final TrackUserData data = getUserData(track);

        if (data != null && data.getForceAnnounce() || this.guildMusicManager.isAnnounceTracks()) {
            // Reset the was from skip status
            if (data != null) {
                data.setForceAnnounce(false);
            }

            guildMusicManager.getLatestChannel()
                .ifPresent((latestChannel) -> AudioTrackKt.toEmbed(
                    track,
                    this.guildMusicManager,
                    getInstance().getShardManager(),
                    false,
                    (message) -> {
                        sendMsg(
                            new MessageConfig.Builder()
                                .setChannel(latestChannel)
                                .setEmbeds(false, message)
                        );

                        return null;
                    }
                ));
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
        final boolean wasForceAnnounce = userData.getForceAnnounce();

        if (this.looping) {
            LOGGER.debug("repeating the current song");

            final Track clone = AudioTrackKt.internalClone(lastTrack);
            storeUserData(clone, copyTrackDataOrCreateNew(lastTrack, wasForceAnnounce));
            this.play(clone);

            return;
        } else if (this.loopingQueue) {
            LOGGER.debug("repeating the queue");
            //Offer it to the queue to prevent the player from playing it
            final Track clone = AudioTrackKt.internalClone(lastTrack);
            storeUserData(clone, copyTrackDataOrCreateNew(lastTrack, wasForceAnnounce));
            queue.offer(clone);
        }

        // clean up the old track's userdata
        removeUserData(lastTrack);

        LOGGER.debug("can start next track");

        skipTrack(wasForceAnnounce);
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

    private TrackUserData copyTrackDataOrCreateNew(Track track, boolean wasForceAnnounce) {
        final TrackUserData oldData = getUserData(track);
        final TrackUserData newData;

        // If we did not have old data (unlikely) we will create it
        if (oldData == null) {
            newData = new TrackUserData(0L);
        } else {
            newData = oldData.copy(oldData.getRequester());
        }

        // Set the was from skip status on the track
        newData.setForceAnnounce(wasForceAnnounce);

        return newData;
    }

    public void onTrackException(Track track, TrackException exception) {
        final String finalCause = Objects.requireNonNullElse(exception.getMessage(), exception.getCause());

        if (finalCause.contains("Something went wrong when decoding the track.")) {
            return;
        }

        final TrackInfo info = track.getInfo();

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
        final var element = Objects.requireNonNull(track.getUserData().get("uuid"));

        return this.userData.get(element.asText());
    }

    public void storeUserData(Track track, TrackUserData data) {
        final var element = Objects.requireNonNull(track.getUserData().get("uuid"));

        this.userData.put(element.asText(), data);
    }

    public void removeUserData(Track track) {
        final var element = Objects.requireNonNull(track.getUserData().get("uuid"));

        this.userData.remove(element.asText());
    }

    private void play(Track track) {
        this.guildMusicManager.getPlayer()
            .update()
            .setTrack(track)
            .subscribe();
    }
}
