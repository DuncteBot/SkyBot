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

import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import me.duncte123.skybot.audio.GuildMusicManager;

import java.util.function.Consumer;

public class MusicEmbedUtils {
    private static final String ICON_PAUSE = "⏸";
    private static final String ICON_PLAY = "▶";
    private static final String ICON_PROGRESS_THUMB = "\uD83D\uDD18";
    private static final char ICON_PROGRESS_BAR = '▬';
    private static final String ICON_SPEAKER_MUTE = "\uD83D\uDD07";
    private static final String ICON_SPEAKER_LOW = "\uD83D\uDD08";
    private static final String ICON_SPEAKER_MED = "\uD83D\uDD09";
    private static final String ICON_SPEAKER_FULL = "\uD83D\uDD0A";
    private static final String ICON_EXPLOSION_EAR = "\uD83D\uDCA5\uD83D\uDC42";

    private MusicEmbedUtils() {}

    public static void createPlayerString(GuildMusicManager mng, Consumer<String> callback) {
        mng.getPlayer().getLavalinkPlayer().ifPresentOrElse((player) -> {
            final TrackInfo trackInfo = player.getTrack().getInfo();
            final long position = player.getPosition();
            final long duration = trackInfo.getLength();

            callback.accept(
                "%s %s `[%s/%s]` %s".formatted(
                    player.getPaused() ? ICON_PAUSE : ICON_PLAY,
                    generateProgressBar((double) position / duration),
                    formatTime(position),
                    formatTime(duration),
                    getVolumeIcon(player.getVolume())
                )
            );
        }, () -> callback.accept("Not playing anything."));
    }

    private static String generateProgressBar(double percent) {
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == (int) (percent * 8)) {
                str.append(ICON_PROGRESS_THUMB);
            } else {
                str.append(ICON_PROGRESS_BAR);
            }
        }
        return str.toString();
    }

    private static String getVolumeIcon(int volume) {
        if (volume == 0) {
            return ICON_SPEAKER_MUTE;
        }

        if (volume < 33) {
            return ICON_SPEAKER_LOW;
        }

        if (volume < 67) {
            return ICON_SPEAKER_MED;
        }

        if (volume > 100) {
            return ICON_EXPLOSION_EAR;
        }

        return ICON_SPEAKER_FULL;
    }

    private static String formatTime(long duration) {
        if (duration == Long.MAX_VALUE) {
            return "LIVE";
        }

        long seconds = Math.round(duration / 1000.0);
        final long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        final long minutes = seconds / 60;
        seconds %= 60;

        return String.format(
            "%s%02d:%02d",
            hours > 0 ? hours + ":" : "",
            minutes,
            seconds
        );
    }
}
