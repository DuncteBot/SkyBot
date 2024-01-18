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

package me.duncte123.skybot.utils;

import dev.arbjerg.lavalink.client.protocol.LavalinkLoadResult;
import dev.arbjerg.lavalink.client.protocol.SearchResult;
import dev.arbjerg.lavalink.client.protocol.Track;
import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongObjectMap;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.audio.AudioLoader;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.objects.AudioData;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AudioUtils {
    private final TLongObjectMap<GuildMusicManager> musicManagers = MapUtils.newLongObjectMap();
    private final Variables variables;

    public AudioUtils(Variables variables) {
        this.variables = variables;
    }

    @Nullable("If the playlist is not a search result")
    public List<Track> searchYoutube(long guildId, String query) {
        final LavalinkLoadResult result = LavalinkManager.INS.getLavalink()
            .getLink(guildId)
            .loadItem("ytsearch:" + query)
            .block();

        if (result instanceof SearchResult playlist) {
            return playlist.getTracks();
        }

        return null;
    }

    public Future<Void> loadAndPlay(final AudioData data, final String trackUrlRaw,
                                    final boolean announce) {
        final boolean isPatron = CommandUtils.isUserTagPatron(data.getUserId());
//        final boolean isPatron = false;
        final String trackUrl;

        //Strip <>'s that prevent discord from embedding link resources
        if (trackUrlRaw.charAt(0) == '<' && trackUrlRaw.endsWith(">")) {
            trackUrl = trackUrlRaw.substring(1, trackUrlRaw.length() - 1);
        } else {
            trackUrl = trackUrlRaw;
        }

        final GuildMusicManager mng = getMusicManager(data.getGuildId());
        final AudioLoader loader = new AudioLoader(data, mng, announce, trackUrl, isPatron);
        final CompletableFuture<Void> future = new CompletableFuture<>();

        LavalinkManager.INS.getLavalink()
            .getLink(data.getGuildId())
            .loadItem(trackUrl)
            .subscribe((result) -> {
                future.complete(null);
                loader.accept(result);
            });

        return future;
    }

    public GuildMusicManager getMusicManager(long guildId) {
        synchronized(this) {
            GuildMusicManager mng = musicManagers.get(guildId);

            if (mng == null) {
                mng = new GuildMusicManager(guildId, variables);
                musicManagers.put(guildId, mng);
            }

            return mng;
        }
    }

    public void removeMusicManager(Guild guild) {
        removeMusicManager(guild.getIdLong());
    }

    public void removeMusicManager(long guildId) {
        synchronized(this) {
            final GuildMusicManager mng = musicManagers.get(guildId);

            if (mng != null) {
                mng.stopAndClear();
                musicManagers.remove(guildId);
            }
        }
    }

    public TLongObjectMap<GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

    public static String getTimestamp(long milliseconds) {
        final int seconds = (int) (milliseconds / 1000) % 60;
        final int minutes = (int) (milliseconds / (1000 * 60)) % 60;
        final int hours = (int) (milliseconds / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
