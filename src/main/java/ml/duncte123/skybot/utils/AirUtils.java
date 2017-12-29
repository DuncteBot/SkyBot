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

import com.wolfram.alpha.WAEngine;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.config.Config;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import ml.duncte123.skybot.objects.Tag;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class AirUtils {

    /**
     * This is our config file
     */
    public static Config config = new ConfigUtils().loadConfig();

    /**
     * The {@link WAEngine engine} to query Wolfram|Alpha
     * This has to be loadded before the commands are loaded
     */
    public static final WAEngine alphaEngine = getWolframEngine();

    /**
     * This will hold the command setup and the registered commands
     */
    public static CommandManager commandManager = new CommandManager();

    /**
     * We are using slf4j to log things to the console
     */
    public static Logger logger = LoggerFactory.getLogger(Settings.defaultName);

    /**
     * This holds the value if we should use a non-SQLite database
     */
    public static boolean nonsqlite = config.getBoolean("use_database", false);

    /**
     * This will store the settings for every guild that we are in
     */
    public static Map<String, GuildSettings> guildSettings = new HashMap<>();

    /**
     * This stores all the tags
     */
    public static Map<String, Tag> tagsList = new TreeMap<>();

    /**
     * This is our audio handler
     */
    public static AudioUtils audioUtils = new AudioUtils();

    /**
     * This helps us to make the coinflip command and the footer quotes work
     */
    public static Random rand = new Random();

    /**
     * This is our database manager, it is a util for the connection
     */
    public static DBManager db = new DBManager();

    /**
     * This converts the online status of a user to a fancy emote
     *
     * @param status The {@link OnlineStatus} to convert
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
     * This will send a message to a channel called modlog
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param time         How long it takes for the punishment to get removed
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, Guild g){
        TextChannel logChannel = getLogChannel(GuildSettingsUtils.getGuild(g).getLogChannel(), g);
        if(logChannel==null || !logChannel.canTalk()) return;
        String length = "";
        if (time != null && !time.isEmpty()) {
            length = " lasting " + time + "";
        }
        
        String punishedUserMention = "<@" + punishedUser.getId() + ">";

        logChannel.sendMessage(EmbedUtils.embedField(punishedUser.getName() + " " + punishment, punishment
                + " by " + mod.getName() + length + (reason.isEmpty()?"":" for " + reason))).queue(
                        msg -> msg.getTextChannel().sendMessage("_Relevant user: " + punishedUserMention + "_").queue()
        );
    }

    /**
     * A version of {@link AirUtils#modLog(User, User, String, String, String, Guild)} but without the time
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, Guild g) {
        modLog(mod, punishedUser, punishment, reason, "", g);
    }

    /**
     * To log a unban or a unmute
     *
     * @param mod          The mod that permed the executeCommand
     * @param unbannedUser The user that the executeCommand is for
     * @param punishment   The type of punishment that got removed
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, Guild g) {
        modLog(mod, unbannedUser, punishment, "", g);
    }

    /**
     * Add the banned user to the database
     *
     * @param modID             The user id from the mod
     * @param userName          The username from the banned user
     * @param userDiscriminator the discriminator from the user
     * @param userId            the id from the banned users
     * @param unbanDate         When we need to unban the user
     * @param guildId           What guild the user got banned in
     */
    public static void addBannedUserToDb(String modID, String userName, String userDiscriminator, String userId, String unbanDate, String guildId) {
        Map<String, Object> postFields = new TreeMap<>();
        postFields.put("modId", modID);
        postFields.put("username", userName);
        postFields.put("discriminator", userDiscriminator);
        postFields.put("userId", userId);
        postFields.put("unbanDate", unbanDate);
        postFields.put("guildId", guildId);
        
        try {
            WebUtils.postRequest(Settings.apiBase + "/ban/json", postFields).close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This will check if there are users that can be unbanned
     *
     * @param jda the current shard manager for this bot
     */
    public static void checkUnbans(ShardManager jda) {
        log("Unban checker", Level.DEBUG, "Checking for users to unban");
        int usersUnbanned = 0;
        Connection database = db.getConnManager().getConnection();
        
        try {
            
            Statement smt = database.createStatement();
            
            ResultSet res = smt.executeQuery("SELECT * FROM " + db.getName() + ".bans");
            
            while (res.next()) {
                java.util.Date unbanDate = res.getTimestamp("unban_date");
                java.util.Date currDate = new java.util.Date();
                
                if (currDate.after(unbanDate)) {
                    usersUnbanned++;
                    log(Level.INFO, "Unbanning " + res.getString("Username"));
                    jda.getGuildCache().getElementById(res.getString("guildId")).getController()
                            .unban(res.getString("userId")).reason("Ban expired").queue();
                    modLog(new ConsoleUser(),
                            new FakeUser(res.getString("Username"),
                                                res.getString("userId"),
                                                res.getString("discriminator")),
                            "unbanned",
                            jda.getGuildById(res.getString("guildId")));
                    database.createStatement().executeQuery("DELETE FROM " + db.getName() + ".bans WHERE id=" + res.getInt("id") + "");
                }
            }
            log("Unban checker", Level.DEBUG, "Checking done, unbanned " + usersUnbanned + " users.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    public static final Pattern URL_REGEX = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/\\/=]*)");

    /**
     * This will validate a link
     *
     * @param url The thing to check
     * @return true or false depending on if the url is valid
     */
    public static boolean isURL(String url) {
        return URL_REGEX.matcher(url).find();
    }

    /**
     * This will check if the number that we are trying to parse is an int
     *
     * @param isint the int to check
     * @return true if it is an int
     */
    public static boolean isInt(String isint) {
        return isint.matches("^\\d+$");
    }

    /**
     * This will convert the VerificationLevel from the guild to how it is displayed in the settings
     *
     * @param lvl The level to convert
     * @return The converted verification level
     */
    // Null safety
    public static String verificationLvlToName(Guild.VerificationLevel lvl) {
        if (Guild.VerificationLevel.LOW.equals(lvl)) {
            return "Low";
        } else if (Guild.VerificationLevel.MEDIUM.equals(lvl)) {
            return "Medium";
        } else if (Guild.VerificationLevel.HIGH.equals(lvl)) {
            return "(╯°□°）╯︵ ┻━┻";
        } else if (Guild.VerificationLevel.VERY_HIGH.equals(lvl)) {
            return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
        }
        return "None";
    }

    /**
     * This converts the game that a user is playing into a nice and readable format
     *
     * @param g the {@link net.dv8tion.jda.core.entities.Game Game} that the {@link net.dv8tion.jda.core.entities.Member Member} has
     * @return a nicely formatted game string
     */
    public static String gameToString(Game g) {
        if (g == null) return "no game";
        
        String gameType = "Playing";
        
        switch (g.getType().getKey()) {
            case 1:
                gameType = "Streaming";
                break;
            case 2:
                gameType = "Listening to";
                break;
            case 3:
                gameType = "Watching";
        }
        
        String gameName = g.getName();
        return gameType + " " + gameName;
    }

    /**
     * Logs a message to the console
     *
     * @param lvl     The {@link Level} to log the message at
     * @param message The message to log
     */
    public static void log(Level lvl, String message) {
        log(Settings.defaultName, lvl, message);
    }

    /**
     * Logs a message to the console
     *
     * @param name    The name of the class that is calling it
     * @param lvl     The {@link Level} to log the message at
     * @param message The message to log
     */
    public static void log(String name, Level lvl, Object message) {
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
        logger = LoggerFactory.getLogger(Settings.defaultName);
    }

    /**
     * This will calculate the bot to user ratio
     *
     * @param g the {@link Guild} that we want to check
     * @return the percentage of users and the percentage of bots in a nice compact array
     */
    public static double[] getBotRatio(Guild g) {
        
        MemberCacheView memberCache = g.getMemberCache();
        double totalCount = memberCache.size();
        double botCount = memberCache.stream().filter(it -> it.getUser().isBot()).count();
        double userCount = totalCount - botCount;
        
        //percent in users
        double userCountP = (userCount / totalCount) * 100;
        
        //percent in bots
        double botCountP = (botCount / totalCount) * 100;
        
        log(Level.DEBUG,
                "In the guild " + g.getName() + "(" + totalCount + " Members), " + userCountP + "% are users, " + botCountP + "% are bots");
        
        return new double[]{Math.round(userCountP), Math.round(botCountP)};
    }

    /**
     * This counts the users in a guild that have an animated avatar
     * @param g the guild to count it in
     * @return the amount users that have a animated avatar in a {@link java.util.concurrent.atomic.AtomicLong AtomicLong} (because why not)
     */
    public static AtomicLong countAnimatedAvatars(Guild g) {

        return new AtomicLong(g.getMemberCache().stream()
                .map(Member::getUser)
                .filter(it -> it.getAvatarId() != null )
	            .filter(it -> it.getAvatarId().startsWith("a_") ).count()
        );
    }

    /**
     * This will get the first channel of a guild that we can write in/should be able to write in
     *
     * @param guild The guild that we want to get the main channel from
     * @return the Text channel that we can send our messages in.
     */
    public static TextChannel getPublicChannel(Guild guild) {
        
        TextChannel pubChann = guild.getTextChannelCache().getElementById(guild.getId());
        
        if (pubChann == null || !pubChann.canTalk()) {
            return guild.getTextChannelCache().stream().filter(TextChannel::canTalk).findFirst().orElse(null);
        }
        
        return pubChann;
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time The time that the bot has been running for
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time) {
        return getUptime(time, false);
    }

    /**
     * This will generate the uptime for us based on the time that we pass in
     *
     * @param time     The time that the bot has been running for
     * @param withTime If we should add the seconds, minutes and hours to the time
     * @return The uptime nicely formatted
     */
    public static String getUptime(long time, boolean withTime) {
        /*
        This code has been inspired from JDA-Butler <https://github.com/Almighty-Alpaca/JDA-Butler/>
         */
        //Like it's ever gonna be up for more then a week
        long years = time / 31104000000L;
        long months = time / 2592000000L % 12;
        long days = time / 86400000L % 30;
        
        //Get the years, months and days
        String uptimeString = "";
        uptimeString += years == 0 ? "" : years + " Year" + (years > 1 ? "s" : "") + ", ";
        uptimeString += months == 0 ? "" : months + " Month" + (months > 1 ? "s" : "") + ", ";
        uptimeString += days == 0 ? "" : days + " Day" + (days > 1 ? "s" : "");
        
        //If we want the time added we pass in true
        if (withTime) {
            long hours = time / 3600000L % 24;
            long minutes = time / 60000L % 60;
            long seconds = time / 1000L % 60;
            
            uptimeString += ", " + (hours == 0 ? "" : hours + " Hour" + (hours > 1 ? "s" : "") + ", ");
            uptimeString += minutes == 0 ? "" : minutes + " Minute" + (minutes > 1 ? "s" : "") + ", ";
            uptimeString += seconds == 0 ? "" : seconds + " Second" + (seconds > 1 ? "s" : "") + " ";
        }
        
        return uptimeString.startsWith(", ") ? uptimeString.replaceFirst(", ", "") : uptimeString;
    }

    /**
     * Gets a Wolfram|Alpha engine for creating and processing queries
     *
     * @return A possibly-null {@link com.wolfram.alpha.WAEngine Wolfram|Alpha engine} instance configured with the
     * token
     */
    private static WAEngine getWolframEngine() {
        WAEngine engine = new WAEngine();
        
        String appId;
        
        appId = config.getString("apis.wolframalpha", "");
        
        if (appId == null || "".equals(appId)) {
            IllegalStateException e
                    = new IllegalStateException("Wolfram Alpha App ID not specified."
                                                + " Please generate one at "
                                                + "https://developer.wolframalpha.com/portal/myapps/");
            logger.error(e.getMessage(), e);
            return null;
        }
        
        engine.setAppID(appId);
        
        engine.setIP("0.0.0.0");
        engine.setLocation("San Francisco");
        engine.setMetric(true);
        engine.setCountryCode("USA");
        
        return engine;
    }

    /**
     * Attempts to load all the tags from the database
     */
    public static void loadAllTags() {
        AirUtils.log(Level.DEBUG, "Loading tags.");
        
        Connection database = db.getConnManager().getConnection();
        try {
            Statement smt = database.createStatement();
            
            ResultSet resultSet = smt.executeQuery("SELECT * FROM " + db.getName() + ".tags");
            
            while (resultSet.next()) {
                String tagName = resultSet.getString("tagName");
                
                tagsList.put(tagName, new Tag(
                                                     resultSet.getInt("id"),
                                                     resultSet.getString("author"),
                                                     resultSet.getString("authorId"),
                                                     tagName,
                                                     resultSet.getString("tagText")
                ));
            }
            
            AirUtils.log(Level.DEBUG, "Loaded " + tagsList.keySet().size() + " tags.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * Attempts to register a new tag
     *
     * @param author The user that created the tag
     * @param tag    the {@link Tag} to add
     * @return True if the tag is added
     */
    public static boolean registerNewTag(User author, Tag tag) {
        if (tagsList.containsKey(tag.getName())) //Return false if the tag is already here
            return false;
        
        Connection database = db.getConnManager().getConnection();
        
        try {
            PreparedStatement statement = database.prepareStatement("INSERT INTO " + db.getName() + ".tags(author ,authorId ,tagName ,tagText) " +
                                                                            "VALUES(? , ? , ? , ?)");
            statement.setString(1, String.format("%#s", author));
            statement.setString(2, author.getId());
            statement.setString(3, tag.getName());
            statement.setString(4, tag.getText());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        
        tagsList.put(tag.getName(), tag);
        return true;
    }

    /**
     * Attempts to delete a tag
     *
     * @param tag the {@link Tag} to delete
     * @return true if the tag is deleted
     */
    public static boolean deleteTag(Tag tag) {
        
        Connection database = db.getConnManager().getConnection();
        
        try {
            PreparedStatement statement = database.prepareStatement("DELETE FROM " + db.getName() + ".tags WHERE tagName= ? ");
            statement.setString(1, tag.getName());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                tagsList.remove(tag.getName());
                database.close();
                return true;
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return false;
    }

    /**
     * This sends a post request to the bot lists with the new guild count
     *
     * @param jda           the jda instance for the token
     * @param newGuildCount the new guild count
     * @return the response from the server
     */
    public static String updateGuildCount(JDA jda, long newGuildCount) {
        Map<String, Object> postFields = new HashMap<>();
        postFields.put("server_count", newGuildCount);
        postFields.put("auth", jda.getToken());
        try {
            return WebUtils.postRequest(Settings.apiBase + "/postGuildCount/json", postFields).body().source().readUtf8();
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public static void updateGuildCountAndCheck(JDA jda, long newGuildCount) {
        JSONObject returnValue = new JSONObject(updateGuildCount(jda, newGuildCount));
        if(returnValue.getString("status").equalsIgnoreCase("failure")) {
            String exceptionMessage = "%s";
            try {
                switch (returnValue.getInt("code")) {
                    case 401: {
                        exceptionMessage = "Unauthorized access! %s";
                        break;
                    }
                    case 400: {
                        exceptionMessage = "Bad request! %s";
                        break;
                    }

                    default: {
                        exceptionMessage = "Server responded with a unknown status message: %s";
                        break;
                    }
                }
            } catch (JSONException ex) {
                throw new UnsupportedOperationException(String.format(exceptionMessage, returnValue.getString("message")), ex);
            }

            throw new UnsupportedOperationException(String.format(exceptionMessage, returnValue.getString("message")));
        }
    }

    /**
     * Stops everything
     */
    public static void stop() {
        try {
            db.getConnManager().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //That just breaks the bot
//        audioUtils.musicManagers.forEach((a, b) ->  b.player.stopTrack());
    }

    /**
     * This gets the channel from a name or id
     * @param channelId the channel name or id
     * @param guild the guild to search in
     * @return the channel
     */
    public static TextChannel getLogChannel(String channelId, Guild guild) {
        if(channelId == null || channelId.isEmpty()) return getPublicChannel(guild);

        TextChannel tc;
        try{
            tc = guild.getTextChannelById(channelId);
        }
        catch (NumberFormatException e) {
            List<TextChannel> tcl = guild.getTextChannelsByName(channelId, true);
            if(tcl.size() > 0) {
                tc = tcl.get(0);
            } else return null;
        }

        return tc;
    }

    /**
     * This generates a random string withe the specified length
     * @param length the length that the string should be
     * @return the generated string
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnpqrstuvwxyz";
        StringBuilder output = new StringBuilder();
        while (output.length() < length) { // length of the random string.
            int index = (int) (rand.nextFloat() * chars.length());
            output.append(chars.charAt(index));
        }
        return output.toString();
    }

    /**
     * Returns a random string that has 10 chars
     * @return a random string that has 10 chars
     */
    public static String generateRandomString() {
        return generateRandomString(10);
    }

    /**
     * Returns a flipped table
     * @return a flipped table
     */
    public static String flipTable() {
        switch (AirUtils.rand.nextInt(4)){
            case 0:
                return "(╯°□°)╯︵┻━┻";
            case 1:
                return "(ノ゜Д゜)ノ︵┻━┻";
            case 2:
                return "(ノಥ益ಥ)ノ︵┻━┻";
            case 3:
               return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
           default:
               return "I CAN'T FLIP THIS TABLE";
        }
    }
}
