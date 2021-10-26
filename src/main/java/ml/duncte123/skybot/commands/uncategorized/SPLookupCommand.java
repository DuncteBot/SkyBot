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

package ml.duncte123.skybot.commands.uncategorized;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import ml.duncte123.skybot.audio.sourcemanagers.DBAudioRef;
import ml.duncte123.skybot.audio.sourcemanagers.spotify.SpotifyAudioSourceManager;
import ml.duncte123.skybot.audio.sourcemanagers.spotify.SpotifyAudioTrack;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.YoutubeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.YoutubeUtils.*;

public class SPLookupCommand extends Command {
    public SPLookupCommand() {
        this.requiresArgs = true;
        this.name = "splookup";
        this.help = "Look up a track from spotify";
        this.usage = "<spotify url>";
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        final var source = ctx.getAudioUtils().getPlayerManager().source(SpotifyAudioSourceManager.class);
        final DBAudioRef reference = new DBAudioRef(ctx.getArgsRaw(), null, false);
        final AudioItem audioItem = source.loadItem(null, reference);

        if (audioItem == null) {
            sendMsg(ctx, "Nothing found for your input");
            return;
        }

        final String googl = ctx.getConfig().apis.googl;

        try {
            if (audioItem instanceof SpotifyAudioTrack track) {
                final AudioTrackInfo info = track.getInfo();
                final List<SearchResult> searchResults = searchYoutubeIdOnly(
                    info.title + ' ' + info.author,
                    googl,
                    5L
                );
                // Convert the results to usable videos
                final var videos = getVideosByIds(
                    searchResults.stream()
                        .map(SearchResult::getId)
                        .map(ResourceId::getVideoId)
                        .collect(Collectors.joining(",")),
                    googl
                );
                final var trackInfos = videos.stream()
                    .map(YoutubeUtils::videoToTrackInfo)
                    .toList();

                if (trackInfos.isEmpty()) {
                    sendMsg(ctx, "Nothing found on youtube?!");
                    return;
                }

                // find the best match
                final long trackDuration = info.length;
                final AudioTrackInfo foundTrack = trackInfos.stream()
                    .min(Comparator.comparingLong(
                        (i) -> Math.abs(i.length - trackDuration)
                    ))
                    .get();

                sendEmbed(
                    ctx,
                    getDefaultEmbed()
                        .setTitle(foundTrack.title)
                        .setAuthor(foundTrack.author)
                        .setDescription(
                            "Watch here: [%1$s]([%1$s)".formatted(foundTrack.uri)
                        )
                        .setThumbnail(getThumbnail(foundTrack.identifier))
                );

            } else if (audioItem instanceof BasicAudioPlaylist) {
                sendMsg(ctx, "Playlists are not supported atm");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMsg(ctx, e.getMessage());
        }
    }
}
