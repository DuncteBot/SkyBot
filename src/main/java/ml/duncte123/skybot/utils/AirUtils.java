package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.db.DataBaseUtil;
import ml.duncte123.skybot.utils.db.DbManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AirUtils {

    /**
     * This contains the guilds that the bot is allowed to join
     */
    public static List<String> whiteList = new ArrayList<>();
    /**
     * This contains the guilds that we don't want the swearfilter/welcome messages to happen
     */
    public static List<String> blackList = new ArrayList<>();
    /**
     * This is our custom logging
     */
    public static CustomLog logger2 = CustomLog.getLog(Config.defaultName);
    /**
     * This helps us to make the coinflip work
     */
    public static Random rand = new Random();

    /**
     * This is our database manager, it is a util for the connection
     */
    public static DbManager db = new DbManager();

    /**
     * This will store the settings for every guild that we are in
     */
    public static HashMap<String, GuildSettings> guildSettings = new HashMap<>();

    /**
     * The default way to display a nice embedded message
     * @param message The message to display
     * @return The {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedMessage(String message) {
        return defaultEmbed().setDescription(message).build();
    }

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
     * The default way to send a embedded image to the channel
     * @param imageURL The url from the image
     * @return The {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} to send to the channel
     */
    public static MessageEmbed embedImage(String imageURL) {
        return defaultEmbed().setImage(imageURL).build();
    }

    /**
     * The default embed layout that all of the embeds are based off
     * @return The way that that the {@link net.dv8tion.jda.core.EmbedBuilder embed} will look like
     */
    public static EmbedBuilder defaultEmbed(){
        return new EmbedBuilder()
                .setColor(Config.defaultColour)
                .setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
    }

    /**
     * This converts the online status of a user to a fancy emote
     * @param status The {@link net.dv8tion.jda.core.OnlineStatus OnlineStatus} to convert
     * @return The fancy converted emote as a mention
     */
    public static String convertStatus(OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return "<:online:313956277808005120>";
            case IDLE:
                return "<:away:313956277220802560>";
            case DO_NOT_DISTURB:
                return "<:dnd:313956276893646850>";

            default:
                return "<:offline:313956277237710868>";
        }
    }

    /**
     * This will fetch the white and black list and put the right id's in the {@link AirUtils#whiteList whitelist} or the {@link AirUtils#blackList blacklist}.
     */
    public static void getWhiteAndBlackList(){
        log(CustomLog.Level.INFO, "Loading black and whitelist.");
        try {

            String dbName = db.getName();

            Connection database = db.getConnection();
            Statement smt = database.createStatement();

            ResultSet resWhiteList = smt.executeQuery("SELECT * FROM " + dbName + ".whiteList");

            while (resWhiteList.next()) {
                whiteList.add(resWhiteList.getString("guildId"));
            }

            ResultSet resBlackList = smt.executeQuery("SELECT * FROM " + dbName + ".blackList");

            while (resBlackList.next()) {
                blackList.add(resBlackList.getString("guildId"));
            }

            log(CustomLog.Level.INFO, "Loaded black and whitelist.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will get the settings from our database and store them in the {@link ml.duncte123.skybot.utils.AirUtils#guildSettings settings}
     */
    public static void loadSettings() {
        log(CustomLog.Level.INFO, "Loading settings.");
        try {
            int guilds = 0;

            String dbName = db.getName();

            Connection database = db.getConnection();
            Statement smt = database.createStatement();

            ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");

            while (resSettings.next()) {
                String guildId = resSettings.getString("guildId");
                boolean enableJoinMsg = resSettings.getBoolean("enableJoinMessage");
                boolean enableSwearFilter = resSettings.getBoolean("enableSwearFilter");

                GuildSettings settings = new GuildSettings(guildId)
                        .setEnableJoinMessage(enableJoinMsg)
                        .setEnableSwearFilter(enableSwearFilter);

                guildSettings.put(guildId, settings);
                guilds++;
            }

            log(CustomLog.Level.INFO, "Loaded settings for "+guilds+" guilds.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateSettings(GuildSettings settings) throws Exception {

    }

    /**
     * This will loop through all of the guild and checks if they are on the whitelist, if not leave the guilds and leave a message
     * @param jda The current instance of the api
     */
    public static void checkGuildsOnWhitelist(JDA jda) {
        for(Guild guild : jda.getGuilds()) {
            if(!whiteList.contains(guild.getId())) {
                log(CustomLog.Level.INFO, "Leaving " + (guild.getName() == null ? "No Name" : guild.getName() ) + ". Guild not on whitelist.");
                guild.getTextChannels().get(0).sendMessage("I'm sorry but this guild is not on the current whitelist, " +
                        "if you want this guild to be on the whitelist please contact _duncte123#1245_!").queue(
                                channel -> channel.getGuild().leave().queueAfter(20, TimeUnit.SECONDS)
                );
            }
        }
    }

    /**
     * This will update the whitelist and the blacklist depending on what <em>whatList</em> is
     * @param guildId The id from the guild to add
     * @param guildName The name from the guild to add
     * @param whatlist What list to add it to
     * @param a1234567890 This is a special auth token
     * @return The response from the api or NULL if the response is "ok"
     */
    public static String insertIntoWhiteOrBlacklist(String guildId, String guildName, String whatlist, String a1234567890) {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "guildID=" + guildId
                    + "&guildName=" + guildName
                    + "&type=" + whatlist
                    + "&a1234567890=" + a1234567890
                    + "&tk=" + SkyBot.jda.getToken().split(" ")[1]
            );
            Request request = new Request.Builder()
                    .url(Config.apiBase + "/updateWhiteAndBlacklist.php")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String returnData = response.body().source().readUtf8();

            response.body().close();

            if(!returnData.equals("ok") ) {
                return returnData;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return "";
    }

    /**
     * THis is a shortcut to insert a guild into the whitelist
     * @param guildId The id from the guild to add
     * @param guildName The name from the guild to add
     * @param a1234567890 This is a special auth token
     * @return The response from the api or NULL if the response is "ok"
     */
    public static String insetIntoWhitelist(String guildId, String guildName, String a1234567890) {
        whiteList.add(guildId);
        return insertIntoWhiteOrBlacklist(guildId, guildName, "whiteList", a1234567890);
    }

    /**
     * THis is a shortcut to insert a guild into the blacklist
     * @param guildId The id from the guild to add
     * @param guildName The name from the guild to add
     * @param a1234567890 This is a special auth token
     * @return The response from the api or NULL if the response is "ok"
     */
    public static String insetIntoBlacklist(String guildId, String guildName, String a1234567890) {
        blackList.add(guildId);
        return insertIntoWhiteOrBlacklist(guildId, guildName, "blackList", a1234567890);
    }

    /**
     * This will send a message to a channel called modlog
     * @param mod The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment The type of punishment
     * @param reason The reason of the punishment
     * @param time How long it takes for the punishment to get removed
     * @param event A instance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, GuildMessageReceivedEvent event){
        String length = "";
        if (!time.isEmpty()) { length = " lasting " + time + ""; }
        String punishedUserMention = "<@" + punishedUser.getId() + ">";
        MessageChannel modLogChannel = event.getGuild().getTextChannelsByName("modlog", true).get(0);
        modLogChannel.sendMessage(embedField(punishedUser.getName() + " " + punishment, punishment
                + " by " + mod.getName() + length + (reason.isEmpty()?"":" for " + reason))).queue(
                        msg -> msg.getTextChannel().sendMessage("_Relevant user: " + punishedUserMention + "_").queue()
        );
    }

    /**
     * A version of {@link AirUtils#modLog(User, User, String, String, String, GuildMessageReceivedEvent)} but without the time
     *
     * @param mod The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment The type of punishment
     * @param reason The reason of the punishment
     * @param event A instance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, GuildMessageReceivedEvent event) {
        modLog(mod, punishedUser, punishment, reason, "", event);
    }

    /**
     * To log a unban or a unmute
     * @param mod The mod that permed the action
     * @param unbannedUser The user that the action is for
     * @param punishment The type of punishment that got removed
     * @param event A instance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, GuildMessageReceivedEvent event) {
        modLog(mod, unbannedUser, punishment, "", event);
    }

    /**
     * Add the banned user to the database
     * @param modID The user id from the mod
     * @param userName The username from the banned user
     * @param userDiscriminator the discriminator from the user
     * @param userId the id from the banned users
     * @param unbanDate When we need to unban the user
     * @param guildId What guild the user got banned in
     */
    public static void addBannedUserToDb(String modID, String userName, String userDiscriminator, String userId, String unbanDate, String guildId) {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "modId=" + modID
                    + "&username=" + userName
                    + "&discriminator=" + userDiscriminator
                    + "&userId=" + userId
                    + "&unbanDate=" + unbanDate
                    + "&guildId=" + guildId
            );
            Request request = new Request.Builder()
                    .url(Config.apiBase + "/ban.php")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            response.body().close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will check if there are users that can be unbanned
     */
    public static void checkUnbans() {
        try {

            String dbName = db.getName();

            Connection database = db.getConnection();
            Statement smt = database.createStatement();

            ResultSet res = smt.executeQuery("SELECT * FROM " + dbName + ".bans");

            while (res.next()) {
                java.util.Date unbanDate = res.getTimestamp("unban_date");
                java.util.Date currDate = new java.util.Date();

                if(currDate.after(unbanDate)) {
                    log(CustomLog.Level.INFO, "Unbanning " + res.getString("Username"));
                    SkyBot.jda.getGuildById(
                            res.getString("guildId")
                    ).getController().unban(
                            res.getString("guildId")
                    ).reason("Ban expired").queue();
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will validate a link
     * @param url The thing to check
     * @return true or false depending on if the url is valid
     */
    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * This will convert the VerificationLevel from the guild to how it is displayed in the settings
     * @param lvl The level to convert
     * @return The converted verification level
     */
    public static String verificationLvlToName(Guild.VerificationLevel lvl){
        if(lvl.equals(Guild.VerificationLevel.LOW)){
            return "Low";
        }else if(lvl.equals(Guild.VerificationLevel.MEDIUM)){
            return "Medium";
        }else if(lvl.equals(Guild.VerificationLevel.HIGH)){
            return "(╯°□°）╯︵ ┻━┻";
        }else if(lvl.equals(Guild.VerificationLevel.VERY_HIGH)){
            return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
        }
        return "none";
    }

    /**
     * Logs a message to the console
     * @param lvl The {@link CustomLog#level level} to log the message at
     * @param message The message to log
     */
    public static void log(CustomLog.Level lvl, String message){
        log(Config.defaultName, lvl, message);
    }

    /**
     * Logs a message to the console
     * @param name The name of the class that is calling it
     * @param lvl The {@link CustomLog#level level} to log the message at
     * @param message The message to log
     */
    public static void log(String name, CustomLog.Level lvl, Object message){
        logger2 = CustomLog.getLog(name);
        logger2.log(lvl, message);
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
     * @param embed the {@link net.dv8tion.jda.core.entities.MessageEmbed MessageEmbed} that we are trying to send
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

    /**
     * This will get the first channel of a guild that we can write in/should be able to write in
     * @param guild The guild that we want to get the main channel from
     * @return the Text channel that we can send our messages in.
     */
    public static TextChannel getPublicChannel(Guild guild) {

        TextChannel pubChann = guild.getTextChannelById(guild.getId());

       if(pubChann==null) {
           return guild.getTextChannels().stream().filter(TextChannel::canTalk).findFirst().orElse(null);
       }

        return pubChann;
    }
}
