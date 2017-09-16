package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.CommandSetup;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.logging.CustomLog;
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.db.DbManager;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Random;

public class AirUtils {

    /**
     * This will hold the command setup and the registered commands
     */
    public static CommandSetup commandSetup = new CommandSetup();
    /**
     * This is our custom logging
     * @deprecated don't use it anymore plx
     */
    @Deprecated
    public static CustomLog logger2 = CustomLog.getLog(Config.defaultName);
    /**
     * This is a new logger using slf4j
     */
    public static Logger logger = LoggerFactory.getLogger(Config.defaultName);
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
     * This is our audio handler
     */
    public static AudioUtils au = new AudioUtils();

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
     * This will get the settings from our database and store them in the {@link ml.duncte123.skybot.utils.AirUtils#guildSettings settings}
     */
    public static void loadSettings() {
        log(Level.INFO, "Loading settings.");

        String dbName = db.getName();

        Connection database = db.getConnection();
        try {
            int guilds = 0;
            Statement smt = database.createStatement();

            ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");

            while (resSettings.next()) {
                String guildId = resSettings.getString("guildId");
                boolean enableJoinMsg = resSettings.getBoolean("enableJoinMessage");
                boolean enableSwearFilter = resSettings.getBoolean("enableSwearFilter");
                String joinmsg = resSettings.getString("customWelcomeMessage");
                String prefix = resSettings.getString("prefix");

                GuildSettings settings = new GuildSettings(guildId)
                        .setEnableJoinMessage(enableJoinMsg)
                        .setEnableSwearFilter(enableSwearFilter)
                        .setCustomJoinMessage(joinmsg)
                        .setCustomPrefix(prefix);

                guildSettings.put(guildId, settings);
                guilds++;
            }

            log(Level.INFO, "Loaded settings for "+guilds+" guilds.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     * @param guild The guild to update it for
     * @param settings the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings) {


        if(!guildSettings.containsKey(settings.getGuildId())) {
            registerNewGuild(guild);
            return;
        }

        String guildId = settings.getGuildId();
        boolean enableJoinMessage = settings.isEnableJoinMessage();
        boolean enableSwearFilter = settings.isEnableSwearFilter();
        String customJoinMessage = settings.getCustomJoinMessage();
        String newPrefix = settings.getCustomPrefix();


        String dbName = db.getName();
        Connection database = db.getConnection();

        try{
            PreparedStatement preparedStatement = database.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
                    "enableJoinMessage= ? , " +
                    "enableSwearFilter= ? ," +
                    "customWelcomeMessage= ? ," +
                    "prefix= ? " +
                    "WHERE guildId='"+guildId+"'");
            preparedStatement.setBoolean(1, enableJoinMessage);
            preparedStatement.setBoolean(2, enableSwearFilter);
            preparedStatement.setString(3, customJoinMessage);
            preparedStatement.setString(4, newPrefix);
            preparedStatement.executeUpdate();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This will register a new guild with their settings on bot join
     * @param g The guild that we are joining
     */
    public static void registerNewGuild(Guild g) {

        if(guildSettings.containsKey(g.getId())) { return; }

        boolean ENABLE_JOIN_MSG = false;
        boolean ENABLE_SWEAR_FILTER = false;
        String defaultMsg = "Welcome {{USER_MENTION}}, to the official {{GUILD_NAME}} guild.";
        GuildSettings newGuildSettings = new GuildSettings(g.getId())
                .setEnableJoinMessage(ENABLE_JOIN_MSG)
                .setEnableSwearFilter(ENABLE_SWEAR_FILTER)
                .setCustomJoinMessage(defaultMsg)
                .setCustomPrefix(Config.prefix);
        guildSettings.put(g.getId(), newGuildSettings);


        String dbName = db.getName();

        Connection database = db.getConnection();

        try {
            Statement smt = database.createStatement();

            smt.execute("INSERT INTO " + dbName + ".guildSettings VALUES(default, '" + g.getId() + "', '"+g.getName()+"', default, default, default, '"+defaultMsg+"')");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will send a message to a channel called modlog
     * @param mod The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment The type of punishment
     * @param reason The reason of the punishment
     * @param time How long it takes for the punishment to get removed
     * @param g A instance of the {@link net.dv8tion.jda.core.entities.Guild guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, Guild g){
        String length = "";
        if (time!=null &&!time.isEmpty()) { length = " lasting " + time + ""; }
        String punishedUserMention = "<@" + punishedUser.getId() + ">";
        MessageChannel modLogChannel = g.getTextChannelsByName("modlog", true).get(0);
        modLogChannel.sendMessage(embedField(punishedUser.getName() + " " + punishment, punishment
                + " by " + mod.getName() + length + (reason.isEmpty()?"":" for " + reason))).queue(
                        msg -> msg.getTextChannel().sendMessage("_Relevant user: " + punishedUserMention + "_").queue()
        );
    }

    /**
     * A version of {@link AirUtils#modLog(User, User, String, String, String, Guild)} but without the time
     *
     * @param mod The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment The type of punishment
     * @param reason The reason of the punishment
     * @param g A instance of the {@link net.dv8tion.jda.core.entities.Guild guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, Guild g) {
        modLog(mod, punishedUser, punishment, reason, "", g);
    }

    /**
     * To log a unban or a unmute
     * @param mod The mod that permed the executeCommand
     * @param unbannedUser The user that the executeCommand is for
     * @param punishment The type of punishment that got removed
     * @param g A instance of the {@link net.dv8tion.jda.core.entities.Guild guild}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, Guild g) {
        modLog(mod, unbannedUser, punishment, "", g);
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
     * @param jda the current shard manager for this bot
     */
    public static void checkUnbans(ShardManager jda) {

        String dbName = db.getName();
        Connection database = db.getConnection();

        try {

            Statement smt = database.createStatement();

            ResultSet res = smt.executeQuery("SELECT * FROM " + dbName + ".bans");

            while (res.next()) {
                java.util.Date unbanDate = res.getTimestamp("unban_date");
                java.util.Date currDate = new java.util.Date();

                if(currDate.after(unbanDate)) {
                    log(Level.INFO, "Unbanning " + res.getString("Username"));
                    jda.getGuildById(
                            res.getString("guildId")
                    ).getController().unban(
                            res.getString("userId")
                    ).reason("Ban expired").queue();
                    modLog(new ConsoleUser(),
                            new FakeUser(res.getString("Username"),
                                    res.getString("userId"), res.getString("discriminator")), "unbanned",
                            jda.getGuildById(res.getString("guildId")));
                    smt.execute("DELETE FROM " + dbName + ".bans WHERE id="+res.getInt("id")+"");
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
     * This will check if the number that we are trying to parse is an int
     * @param isint the int to check
     * @return true if it is an int
     */
    public static boolean isInt(String isint) {
        try {
            Integer.parseInt(isint);
            return true;
        }
        catch (NumberFormatException e) {
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
     * @param lvl The {{@link org.slf4j.event.Level level} to log the message at
     * @param message The message to log
     */
    public static void log(Level lvl, String message){
        log(Config.defaultName, lvl, message);
    }

    /**
     * Logs a message to the console
     * @param name The name of the class that is calling it
     * @param lvl The {@link org.slf4j.event.Level level} to log the message at
     * @param message The message to log
     */
    public static void log(String name, Level lvl, Object message){
        logger = LoggerFactory.getLogger(name);

        String msg = String.valueOf(message);

        switch (lvl) {
            case ERROR:
                logger.error(msg);
                break;
            case WARN:
                logger.warn(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case DEBUG:
                logger.debug(msg);
                break;
            case TRACE:
                logger.trace(msg);
                break;
        }
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
     * This will calculate the bot to user ratio
     * @param g the {@link net.dv8tion.jda.core.entities.Guild Guild} that we want to check
     * @return the percentage of users and the percentage of bots in a nice compact array
     */
    public static double[] getBotRatio(Guild g) {

        double totalCount = g.getMembers().size();
        double botCount = 0;
        double userCount = 0;

        for(Member m : g.getMembers()) {
            if(m.getUser().isBot()) {
                botCount++;
            } else {
                userCount++;
            }
        }

        //percent in users
        double userCountP = (userCount/totalCount)*100;

        //percent in bots
        double botCountP = (botCount/totalCount)*100;

        log(Level.INFO,
                "In the guild " + g.getName() + "("+totalCount+" Members), " +userCountP+ "% are users, " +botCountP+ "% are bots");

        return new double[] {Math.round(userCountP), Math.round(botCountP)};
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
