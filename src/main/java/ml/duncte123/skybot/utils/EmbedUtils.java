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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.audio.GuildMusicManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple class to help me build embeds
 */
public class EmbedUtils {

    // Quote, User
    public static Map<String, String> footerQuotes = new HashMap<>();

    /**
     * The default way to send a embedded message to the channel with a field in it
     *
     * @param title   The title of the field
     * @param message The message to display
     * @return The {@link MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedField(String title, String message) {
        return defaultEmbed().addField(title, message, false).build();
    }

    /**
     * The default way to display a nice embedded message
     *
     * @param message The message to display
     * @return The {@link MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedMessage(String message) {
        return defaultEmbed().setDescription(message).build();
    }

    /**
     * The default way to send a embedded image to the channel
     *
     * @param imageURL The url from the image
     * @return The {@link MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedImage(String imageURL) {
        return defaultEmbed().setImage(imageURL).build();
    }

    /**
     * The default embed layout that all of the embeds are based off
     *
     * @return The way that that the {@link EmbedBuilder embed} will look like
     */
    public static EmbedBuilder defaultEmbed() {
        EmbedBuilder eb = new EmbedBuilder()
                                  .setColor(Settings.defaultColour);
        if (AirUtils.nonsqlite) {
            //Get a random index from the quotes
            int randomIndex = AirUtils.rand.nextInt(footerQuotes.size());
            //Get the quote as a string
            String quote = String.valueOf(footerQuotes.keySet().toArray()[randomIndex]);
            String user = String.valueOf(footerQuotes.values().toArray()[randomIndex]);
            String finalQuote = StringUtils.abbreviate(quote, 100) + " - " + user;
            //Set the quote in the footer
            eb.setFooter(finalQuote, Settings.defaultIcon);
        } else {
            eb.setFooter(Settings.defaultName, Settings.defaultIcon)
                    .setTimestamp(Instant.now());
        }
        return eb;
    }

    /**
     * This will generate a nice player embed for us
     *
     * @param mng the {@link net.dv8tion.jda.core.entities.Guild} that we need the info for
     * @return the String that we can place in our embed
     */
    public static String playerEmbed(GuildMusicManager mng) {
        return (mng.player.isPaused() ? "\u23F8" : "\u25B6") + " " +
                       generateProgressBar((double) mng.player.getPlayingTrack().getPosition() / mng.player.getPlayingTrack().getDuration())
                       + " `[" + formatTime(mng.player.getPlayingTrack().getPosition()) + "/" + formatTime(mng.player.getPlayingTrack().getDuration()) + "]` "
                       + getVolumeIcon(mng.player.getVolume());
    }

    /**
     * This will calculate the progressbar for us
     *
     * @param percent how far we are in the audio track
     * @return the progressbar
     */
    public static String generateProgressBar(double percent) {
        StringBuilder str = new StringBuilder();
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
     * @param volume the volume of our player
     * @return the volume icon emote
     */
    public static String getVolumeIcon(int volume) {
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
     * @param duration how far we are in the track
     * @return our formatted time
     */
    public static String formatTime(long duration) {
        if (duration == Long.MAX_VALUE) {
            return "LIVE";
        }
        long seconds = Math.round(duration / 1000.0);
        long hours = seconds / (60 * 60);
        seconds %= 60 * 60;
        long minutes = seconds / 60;
        seconds %= 60;
        return (hours > 0 ? hours + ":" : "") + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    /**
     * This will convert our embeds for if the bot is not able to send embeds
     *
     * @param embed the {@link MessageEmbed} that we are trying to send
     * @return the converted embed
     */
    public static String embedToMessage(MessageEmbed embed) {
        StringBuilder msg = new StringBuilder();
        
        if (embed.getAuthor() != null) {
            msg.append("***").append(embed.getAuthor().getName()).append("***\n\n");
        }
        if (embed.getDescription() != null) {
            msg.append("_").append(embed.getDescription()).append("_\n\n");
        }
        for (MessageEmbed.Field f : embed.getFields()) {
            msg.append("__").append(f.getName()).append("__\n").append(f.getValue()).append("\n\n");
        }
        if (embed.getImage() != null) {
            msg.append(embed.getImage().getUrl()).append("\n");
        }
        if (embed.getFooter() != null) {
            msg.append(embed.getFooter().getText());
        }
        if (embed.getTimestamp() != null) {
            msg.append(" | ").append(embed.getTimestamp());
        }
        
        return msg.toString();
    }
}
