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

package ml.duncte123.skybot.audio;

import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.OrderedExecutor;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.common.tools.ExecutorTools;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.audio.sourcemanagers.spotify.SpotifyAudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.FAULT;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class UserContextAudioPlayerManager extends DefaultAudioPlayerManager {

    private static final int MAXIMUM_LOAD_REDIRECTS = 5;

    private static final Logger log = LoggerFactory.getLogger(UserContextAudioPlayerManager.class);

    // Executors
    private final Supplier<OrderedExecutor> orderedInfoExecutor = () -> getField("orderedInfoExecutor");

    private final Supplier<List<AudioSourceManager>> sourceManagers = () -> getField("sourceManagers");

    @Override
    public void registerSourceManager(AudioSourceManager sourceManager) {
        // TODO: temp remove when reddit is on all lavalink nodes
        if (sourceManager instanceof RedditAudioSourceManager) {
            return;
        }

        super.registerSourceManager(sourceManager);
    }

    public Future<Void> loadItemOrdered(final Object orderingKey, final String identifier,
                                        final AudioLoadResultHandler resultHandler, final boolean isPatron) {

        try {
            return orderedInfoExecutor.get().submit(orderingKey, createItemLoader(identifier, resultHandler, isPatron));
        } catch (RejectedExecutionException e) {
            return handleLoadRejected(identifier, resultHandler, e);
        }
    }

    private Future<Void> handleLoadRejected(String identifier, AudioLoadResultHandler resultHandler, RejectedExecutionException e) {
        final FriendlyException exception = new FriendlyException("Cannot queue loading a track, queue is full.", SUSPICIOUS, e);
        ExceptionTools.log(log, exception, "queueing item " + identifier);

        resultHandler.loadFailed(exception);

        return ExecutorTools.COMPLETED_VOID;
    }

    private Callable<Void> createItemLoader(final String identifier, final AudioLoadResultHandler resultHandler, boolean isPatron) {
        return () -> {
            final boolean[] reported = new boolean[1];

            try {
                if (!checkSourcesForItem(new AudioReference(identifier, null), resultHandler, reported, isPatron)) {
                    log.debug("No matches for track with identifier {}.", identifier);
                    resultHandler.noMatches();
                }
            } catch (Throwable throwable) {
                if (reported[0]) {
                    log.warn("Load result handler for {} threw an exception", identifier, throwable);
                } else {
                    dispatchItemLoadFailure(identifier, resultHandler, throwable);
                }

                ExceptionTools.rethrowErrors(throwable);
            }

            return null;
        };
    }

    private void dispatchItemLoadFailure(String identifier, AudioLoadResultHandler resultHandler, Throwable throwable) {
        final FriendlyException exception = ExceptionTools.wrapUnfriendlyExceptions("Something went wrong when looking up the track", FAULT, throwable);

        if (!(throwable instanceof LimitReachedException)) {
            ExceptionTools.log(log, exception, "loading item " + identifier);
        }

        resultHandler.loadFailed(exception);
    }

    private boolean checkSourcesForItem(AudioReference reference, AudioLoadResultHandler resultHandler, boolean[] reported, boolean isPatron) {
        AudioReference currentReference = reference;

        for (int redirects = 0; redirects < MAXIMUM_LOAD_REDIRECTS && currentReference.identifier != null; redirects++) {
            final AudioItem item = checkSourcesForItemOnce(currentReference, resultHandler, reported, isPatron);
            if (item == null) {
                return false;
            } else if (!(item instanceof AudioReference)) {
                return true;
            }
            currentReference = (AudioReference) item;
        }

        return false;
    }

    private AudioItem checkSourcesForItemOnce(AudioReference reference, AudioLoadResultHandler resultHandler, boolean[] reported, boolean isPatron) {
        for (final AudioSourceManager sourceManager : sourceManagers.get()) {
            if (reference.containerDescriptor != null && !(sourceManager instanceof ProbingAudioSourceManager)) {
                continue;
            }

            final AudioItem item;

            if (sourceManager instanceof SpotifyAudioSourceManager) {
                item = ((SpotifyAudioSourceManager) sourceManager).loadItem(reference, isPatron);
            } else {
                item = sourceManager.loadItem(this, reference);
            }

            if (item != null) {
                if (item instanceof AudioTrack) {
                    log.debug("Loaded a track with identifier {} using {}.", reference.identifier, sourceManager.getClass().getSimpleName());
                    reported[0] = true;
                    resultHandler.trackLoaded((AudioTrack) item);
                } else if (item instanceof AudioPlaylist) {
                    log.debug("Loaded a playlist with identifier {} using {}.", reference.identifier, sourceManager.getClass().getSimpleName());
                    reported[0] = true;
                    resultHandler.playlistLoaded((AudioPlaylist) item);
                }
                return item;
            }
        }

        return null;
    }

    private <T> T getField(String name) {
        final Class<?> klass = this.getClass().getSuperclass();

        try {
            final Field field = klass.getDeclaredField(name);
            field.setAccessible(true);

            //noinspection unchecked
            return (T) field.get(this);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();

            return null;
        }
    }
}
