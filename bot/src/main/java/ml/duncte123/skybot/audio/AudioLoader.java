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
import dev.arbjerg.lavalink.protocol.v4.*;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.commands.music.RadioCommand;
import ml.duncte123.skybot.exceptions.LimitReachedException;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.AudioData;
import ml.duncte123.skybot.objects.RadioStream;
import ml.duncte123.skybot.objects.TrackUserData;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.*;
import java.util.function.Consumer;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class AudioLoader implements Consumer<LoadResult> {

    private final AudioData data;
    private final long requester;
    private final GuildMusicManager mng;
    private final boolean announce;
    private final String trackUrl;
    private final boolean isPatron;

    public AudioLoader(AudioData data, GuildMusicManager mng, boolean announce, String trackUrl, boolean isPatron) {
        this.data = data;
        this.requester = data.getUserId();
        this.mng = mng;
        this.announce = announce;
        this.trackUrl = trackUrl;
        this.isPatron = isPatron;
    }

    @Override
    public void accept(LoadResult loadResult) {
        if (loadResult instanceof LoadResult.TrackLoaded trackLoaded) {
            this.trackLoaded(trackLoaded);
        } else if (loadResult instanceof LoadResult.PlaylistLoaded playlistLoaded) {
            this.playlistLoaded(playlistLoaded);
        } else if (loadResult instanceof LoadResult.SearchResult searchResult) {
            this.searchLoaded(searchResult);
        } else if (loadResult instanceof LoadResult.NoMatches) {
            this.noMatches();
        } else if (loadResult instanceof LoadResult.LoadFailed loadFailed) {
            this.loadFailed(loadFailed.getData());
        }
    }

    private void trackLoaded(LoadResult.TrackLoaded data) {
        final Track track = data.getData().copyWithUserData(new JsonObject(Map.of(
            "uuid", JsonElementKt.JsonPrimitive(UUID.randomUUID().toString())
        )));

        mng.getScheduler().storeUserData(track, new TrackUserData(this.requester));

        final TrackScheduler scheduler = this.mng.getScheduler();

        if (!this.isPatron && !scheduler.canQueue()) {
            sendMsg(new MessageConfig.Builder()
                .setChannel(this.data.getChannel())
                .replyTo(this.data.getReplyToMessage())
                .setMessage(String.format("Could not queue track because limit of %d tracks has been reached.\n" +
                    "Consider supporting us on patreon to queue up unlimited songs.", TrackScheduler.MAX_QUEUE_SIZE))
                .build());
            return;
        }

        try {
            scheduler.addToQueue(track, this.isPatron);

            if (this.announce) {
                final TrackInfo info = track.getInfo();
                final String uri = info.getUri();

                final String title = getSteamTitle(track, info.getTitle(), this.data.getVariables().getCommandManager());
                final String msg = "Adding to queue: [" + StringKt.abbreviate(title, 500) + "](" + uri + ')';

                sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(this.data.getChannel())
                        .replyTo(this.data.getReplyToMessage())
                        .setEmbeds(embedMessage(msg)
                            .setThumbnail(track.getInfo().getArtworkUrl()))
                        .build()
                );
            }
        }
        catch (LimitReachedException e) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(this.data.getChannel())
                    .replyTo(this.data.getReplyToMessage())
                    .setMessage(String.format("You exceeded the maximum queue size of %s tracks", e.getSize()))
                    .build()
            );
        }
    }

    private void playlistLoaded(LoadResult.PlaylistLoaded playlistLoaded) {
        final Playlist playlist = playlistLoaded.getData();
        final List<Track> tracks = playlist.getTracks();

        if (tracks.isEmpty()) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(this.data.getChannel())
                    .replyTo(this.data.getReplyToMessage())
                    .setEmbeds(embedMessage("Error: This playlist is empty."))
                    .build()
            );

            return;
        }

        final PlaylistInfo playlistInfo = playlist.getInfo();

        try {
            final TrackScheduler trackScheduler = this.mng.getScheduler();

            List<Track> tracksRaw = tracks;
            final int selectedTrackIndex = playlistInfo.getSelectedTrack();

            if (selectedTrackIndex > -1) {
                tracksRaw = tracksRaw.subList(selectedTrackIndex, tracksRaw.size());
            }

            final List<Track> limitedTracks = tracksRaw.stream().map((track) -> {
                track = track.copyWithUserData(new JsonObject(Map.of(
                    "uuid", JsonElementKt.JsonPrimitive(UUID.randomUUID().toString())
                )));

                mng.getScheduler().storeUserData(track, new TrackUserData(this.requester));

                return track;
            }).toList();

            for (final Track track : limitedTracks) {
                trackScheduler.addToQueue(track, this.isPatron);
            }

            if (this.announce) {
                // TODO: find a way to fix up BigChungusPlaylist
                final String msg = getPlaylistMsg(tracks, playlistInfo);

                sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(this.data.getChannel())
                        .replyTo(this.data.getReplyToMessage())
                        .setEmbeds(embedMessage(msg))
                        .build()
                );
            }
        }
        catch (LimitReachedException e) {
            if (this.announce) {
                sendMsg(
                    new MessageConfig.Builder()
                        .setChannel(this.data.getChannel())
                        .replyTo(this.data.getReplyToMessage())
                        .setMessage(String.format("The first %s tracks from %s have been queued up\n" +
                            "Consider supporting us on patreon to queue up unlimited songs.", e.getSize(), playlistInfo.getName()))
                        .build()
                );
            }

        }
    }

    private String getPlaylistMsg(List<Track> tracks, PlaylistInfo playlistInfo) {
        final String sizeMsg = String.valueOf(tracks.size());

        /*if (playlist instanceof BigChungusPlaylist bigBoi && bigBoi.isBig()) {
            sizeMsg = tracks.size() + "/" + bigBoi.getOriginalSize();
        } else {
            sizeMsg = String.valueOf(tracks.size());
        }*/

        return String.format(
            "Adding **%s** tracks to the queue from **%s**",
            sizeMsg,
            playlistInfo.getName()
        );
    }

    private void searchLoaded(LoadResult.SearchResult searchResult) {
        System.out.println("WARNING A SEARCH RESULT WAS TRIGGERED " + searchResult);
    }

    private void noMatches() {
        if (this.announce) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(this.data.getChannel())
                    .replyTo(this.data.getReplyToMessage())
                    .setEmbeds(embedMessage("Nothing found by *" + StringKt.abbreviate(this.trackUrl, MessageEmbed.VALUE_MAX_LENGTH) + '*'))
                    .build()
            );
        }
    }

    private void loadFailed(Exception exception) {
        /*if (exception.getCause() != null && exception.getCause() instanceof final LimitReachedException cause) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(this.data.getChannel())
                    .replyTo(this.data.getReplyToMessage())
                    .setMessage(
                        String.format("%s, maximum of %d tracks exceeded", cause.getMessage(), cause.getSize())
                    )
                    .build()
            );

            return;
        }*/

        if (!this.announce) {
            return;
        }

        final String finalCause = Objects.requireNonNullElse(exception.getMessage(), exception.getCause());

        if (finalCause.endsWith("Playback on other websites has been disabled by the video owner.")) {
            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(this.data.getChannel())
                    .replyTo(this.data.getReplyToMessage())
                    .setEmbeds(
                        embedMessage("Could not play: " + this.trackUrl
                            + "\nExternal playback of this video was blocked by YouTube.")
                    )
                    .build()
            );
            return;
        }

        sendMsg(
            new MessageConfig.Builder()
                .setChannel(this.data.getChannel())
                .replyTo(this.data.getReplyToMessage())
                .setEmbeds(
                    embedMessage("Could not play: " + StringKt.abbreviate(finalCause, MessageEmbed.VALUE_MAX_LENGTH)
                        + "\nIf this happens often try another link or join our [discord server](https://duncte.bot/server) to get help!")
                )
                .build()
        );

    }

    private static String getSteamTitle(Track track, String rawTitle, CommandManager commandManager) {
        String title = rawTitle;

        if (track.getInfo().isStream()) {
            final Optional<RadioStream> stream = ((RadioCommand) commandManager.getCommand("radio"))
                .getRadioStreams()
                .stream()
                .filter(s -> s.getUrl().equals(track.getInfo().getUri())).findFirst();

            if (stream.isPresent()) {
                title = stream.get().getName();
            }
        }

        return title;
    }
}
