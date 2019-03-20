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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.GuildMusicManager;

/**
 * A simple class to help me build embeds
 */
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class MusicEmbedUtils {

    /**
     * This will generate a nice player embed for us
     *
     * @param mng
     *         the {@link net.dv8tion.jda.core.entities.Guild} that we need the info for
     *
     * @return the String that we can place in our embed
     */
    public static String playerEmbed(GuildMusicManager mng) {
        return (mng.player.isPaused() ? "\u23F8" : "\u25B6") + " " +
            generateProgressBar((double) mng.player.getTrackPosition() / mng.player.getPlayingTrack().getDuration())
            + " `[" + formatTime(mng.player.getTrackPosition()) + "/" + formatTime(mng.player.getPlayingTrack().getDuration()) + "]` "
            + getVolumeIcon(mng.player.getVolume());
    }

    /**
     * This will calculate the progressbar for us
     *
     * @param percent
     *         how far we are in the audio track
     *
     * @return the progressbar
     */
    private static String generateProgressBar(double percent) {
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == (int) (percent * 8)) {
                str.append("\uD83D\uDD18");
            } else {
                str.append("▬");
            }
        }
        return str.toString();
    }

    /**
     * This will give a nice emote depending on how loud we are sending the music
     *
     * @param volume
     *         the volume of our player
     *
     * @return the volume icon emote
     */
    private static String getVolumeIcon(int volume) {
        if (volume == 0) {
            return "\uD83D\uDD07";
        }
        if (volume < 33) {
            return "\uD83D\uDD08";
        }
        if (volume < 67) {
            return "\uD83D\uDD09";
        }
        return "\uD83D\uDD0A";
    }

    /**
     * This wil format our current player time in this format: hh:mm:ss
     *
     * @param duration
     *         how far we are in the track
     *
     * @return our formatted time
     */
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
