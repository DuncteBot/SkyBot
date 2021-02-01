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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.audio.GuildMusicManager;

public class MusicEmbedUtils {

    private MusicEmbedUtils() {}

    public static String playerEmbed(GuildMusicManager mng) {
        return (mng.player.isPaused() ? "\u23F8" : "\u25B6") + " " +
            generateProgressBar((double) mng.player.getTrackPosition() / mng.player.getPlayingTrack().getDuration())
            + " `[" + formatTime(mng.player.getTrackPosition()) + "/" + formatTime(mng.player.getPlayingTrack().getDuration()) + "]` "
            + getVolumeIcon(mng.player.getFilters().getVolume());
    }

    private static String generateProgressBar(double percent) {
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == (int) (percent * 8)) {
                str.append("\uD83D\uDD18");
            } else {
                str.append('▬');
            }
        }
        return str.toString();
    }

    private static String getVolumeIcon(float volume) {
        if (volume == 0.0) {
            return "\uD83D\uDD07";
        }

        if (volume < 0.33) {
            return "\uD83D\uDD08";
        }

        if (volume < 0.67) {
            return "\uD83D\uDD09";
        }

        if (volume > 1) {
            // explosion ear
            return "\uD83D\uDCA5\uD83D\uDC42";
        }

        return "\uD83D\uDD0A";
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
