package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.audio.GuildMusicManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple class to help me build embeds
 */
public class EmbedUtils {

                    // Quote, User
    public static Map<String,String> footerQuotes = new HashMap<>();

    /**
     * The default way to send a embedded message to the channel with a field in it
     * @param title The title of the field
     * @param message The message to display
     * @return The {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedField(String title, String message){
        return defaultEmbed().addField(title, message, false).build();
    }

    /**
     * The default way to display a nice embedded message
     * @param message The message to display
     * @return The {@link MessageEmbed MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedMessage(String message) {
        return defaultEmbed().setDescription(message).build();
    }

    /**
     * The default way to send a embedded image to the channel
     * @param imageURL The url from the image
     * @return The {@link MessageEmbed MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedImage(String imageURL) {
        return defaultEmbed().setImage(imageURL).build();
    }

    /**
     * The default embed layout that all of the embeds are based off
     * @return The way that that the {@link net.dv8tion.jda.core.EmbedBuilder embed} will look like
     */
    public static EmbedBuilder defaultEmbed(){
        //Get a random index from the quotes
        int randomIndex = AirUtils.rand.nextInt(footerQuotes.size());
        //Get the quote as a string
        String quote = String.valueOf(footerQuotes.keySet().toArray()[randomIndex]);
        String user = String.valueOf(footerQuotes.values().toArray()[randomIndex]);
        String finalQuote = quote + " - " + user;

        return new EmbedBuilder()
                .setColor(Settings.defaultColour)
                //Set the quote in the footer
                .setFooter(finalQuote, Settings.defaultIcon);
                //.setFooter(Settings.defaultName, Settings.defaultIcon)
                //.setTimestamp(Instant.now());
    }

    /**
     * This will generate a nice player embed for us
     * @param mng the {@link net.dv8tion.jda.core.entities.Guild Guild} that we need the info for
     * @return the String that we can place in our embed
     */
    public static String playerEmbed(GuildMusicManager mng) {

        return (mng.player.isPaused()?"\u23F8":"\u25B6")+" "+
                generateProgressBar((double)mng.player.getPlayingTrack().getPosition()/mng.player.getPlayingTrack().getDuration())
                +" `["+formatTime(mng.player.getPlayingTrack().getPosition()) + "/" + formatTime(mng.player.getPlayingTrack().getDuration()) +"]` "
                + getVolumeIcon(mng.player.getVolume());
    }

    /**
     * This will calculate the progressbar for us
     * @param percent how far we are in the audio track
     * @return the progressbar
     */
    public static String generateProgressBar(double percent) {
        String str = "";
        for(int i=0; i<8; i++) {
            if (i == (int) (percent * 8)) {
                str += "\uD83D\uDD18";
            } else {
                str += "▬";
            }
        }
        return str;
    }

    /**
     * This will give a nice emote depending on how loud we are sending the music
     * @param volume the volume of our player
     * @return the volume icon emote
     */
    public static String getVolumeIcon(int volume) {
        if(volume == 0) {
            return "\uD83D\uDD07";
        }
        if(volume < 30) {
            return "\uD83D\uDD08";
        }
        if(volume < 70) {
            return "\uD83D\uDD09";
        }
        return "\uD83D\uDD0A";
    }

    /**
     * This wil format our current player time in this format: hh:mm:ss
     * @param duration how far we are in the track
     * @return our formatted time
     */
    public static String formatTime(long duration) {
        if(duration == Long.MAX_VALUE) {
            return "LIVE";
        }
        long seconds = Math.round(duration/1000.0);
        long hours = seconds/(60*60);
        seconds %= 60*60;
        long minutes = seconds/60;
        seconds %= 60;
        return (hours>0 ? hours+":" : "") + (minutes<10 ? "0"+minutes : minutes) + ":" + (seconds<10 ? "0"+seconds : seconds);
    }

    /**
     * This will convert our embeds for if the bot is not able to send embeds
     * @param embed the {@link MessageEmbed MessageEmbed} that we are trying to send
     * @return the converted embed
     */
    public static String embedToMessage(MessageEmbed embed) {

        String msg = "";

        if(embed.getAuthor() != null) {
            msg += "***"+embed.getAuthor().getName()+"***\n\n";
        }
        if(embed.getDescription()!=null) {
            msg += "_"+embed.getDescription()+"_\n\n";
        }
        for(MessageEmbed.Field f : embed.getFields()) {
            msg += "__"+f.getName()+"__\n"+f.getValue()+"\n\n";
        }
        if(embed.getImage()!=null) {
            msg+= embed.getImage().getUrl();
        }
        if(embed.getFooter()!=null) {
            msg += embed.getFooter().getText();
        }
        if(embed.getTimestamp() != null) {
            msg += "|"+embed.getTimestamp();
        }

        return msg;
    }
}
