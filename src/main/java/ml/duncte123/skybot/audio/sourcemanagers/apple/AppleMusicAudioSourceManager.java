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

package ml.duncte123.skybot.audio.sourcemanagers.apple;

import com.fasterxml.jackson.databind.JsonNode;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import ml.duncte123.skybot.audio.SupportsPatron;
import okhttp3.Request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// yikes, this costs €99/year
public class AppleMusicAudioSourceManager implements AudioSourceManager, SupportsPatron {
    // https://developer.apple.com/documentation/applemusicapi/

    // playlist: https://music.apple.com/nl/playlist/lofi-rap/pl.u-4JomXd3CXxPgX2g?l=en
    // album: https://music.apple.com/nl/album/sparkle-mountain-single/1525954043
    // Artist: https://music.apple.com/nl/artist/andrew-huang/130057628?l=en
    // songs seem to have the i parameter for that
    // Song: https://music.apple.com/nl/album/nintendo-before-school/1445190129?i=1445190410&l=en

    private static final String BASE_URL = "https:\\/\\/(?:music|itunes)\\.apple\\.com\\/([a-zA-Z0-9-]{2,})\\/";
    private static final String REST_REGEX = "(?:.*)";

    private static final String ALBUM_PART = "\\/album\\/[a-zA-Z0-9-]+\\/([0-9]+)";
    private static final String SONG_ID_PART = "[?&]i=([a-zA-Z0-9.-]+)";
    private static final String ARTIST_PART = "\\/artist\\/[a-zA-Z0-9-]+\\/([0-9]+)";
    private static final String PLAYLIST_PART = "\\/playlist\\/[a-zA-Z0-9-]+\\/([a-zA-Z0-9.-]+)";

    private static final Pattern SONG_ID_REGEX = Pattern.compile(SONG_ID_PART);
    private static final Pattern ALBUM_REGEX = Pattern.compile("^(" + BASE_URL + ALBUM_PART + ")" + REST_REGEX +"$");
    private static final Pattern ARTIST_REGEX = Pattern.compile("^(" + BASE_URL + ARTIST_PART + ")" + REST_REGEX +"$");
    private static final Pattern PLAYLIST_REGEX = Pattern.compile("^(" + BASE_URL + PLAYLIST_PART + ")" + REST_REGEX +"$");

    @Override
    public String getSourceName() {
        return "apple_music";
    }

    @Override
    public AudioItem loadItem(DefaultAudioPlayerManager manager, AudioReference reference) {
        return loadItem(reference, false);
    }

    @Override
    public AudioItem loadItem(AudioReference reference, boolean isPatron) {
        AudioItem item = getAppleAlbum(reference, isPatron);

        // todo: playlists and artists

        return item;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // No encoding needed
    }

    // TODO
    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {
        // TODO
    }

    @Nullable
    private AudioItem getAppleAlbum(AudioReference reference, boolean isPatron) {
        final String identifier = reference.identifier;
        final Matcher matcher = ALBUM_REGEX.matcher(identifier);

        if (!matcher.matches()) {
            return null;
        }

        final String storeFront = matcher.group(1);
        final Matcher trackIdMatcher = SONG_ID_REGEX.matcher(identifier);

        if (trackIdMatcher.find()) {
            final String songId = trackIdMatcher.group(trackIdMatcher.groupCount());

            return getAppleSong(storeFront, songId);
        }

        final String albumId = matcher.group(matcher.groupCount());

        return null;
    }

    @Nullable
    private AudioItem getAppleSong(@Nonnull String storeFront, @Nonnull String songId) {
        final String path = String.format("catalog/%s/songs/%s", storeFront, songId);

        return null;
    }

    @Nullable
    private JsonNode fetchData(@Nonnull String path) throws IOException {
        final String url = "https://api.music.apple.com/v1/" + path;

        return null;
    }

    private Request.Builder getBaseRequest(@Nonnull String url) {
        return new Request.Builder()
            .get()
            .url(url)
            .header("Authorization", "Bearer [developer token]")
            ;
    }
}
