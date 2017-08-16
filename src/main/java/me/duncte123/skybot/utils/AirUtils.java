package me.duncte123.skybot.utils;

import me.duncte123.skybot.SkyBot;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AirUtils {

    public static List<String> whiteList = new ArrayList<>();
    public static List<String> blackList = new ArrayList<>();
    public static CustomLog logger2 = CustomLog.getLog(Config.defaultName);

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

    public static String convertStatus(OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return "<:online:313956277808005120>";
            case IDLE:
                return "<:away:313956277220802560>";
            case DO_NOT_DISTURB:
                return "<:dnd:313956276893646850>";
            case OFFLINE:
                return "<:offline:313956277237710868>";
            case INVISIBLE:
                return "<:dnd:313956276893646850>";
            case UNKNOWN:
                return "<:blobnomcookie_secret:317636549342789632>";

            default:
                return "<:offline:313956277237710868>";
        }
    }

    public static void getWhiteAndBlackList(){
        log(CustomLog.Level.INFO, "Loading black and whitelist.");
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "delete=true");
            Request request = new Request.Builder()
                    .url(Config.apiBase + "/getWhiteAndBlacklist.php")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonData = response.body().source().readUtf8();response.body().source().readUtf8();
            JSONArray json = new JSONArray(jsonData);
            for(Object userJson : json) {
                JSONObject listData = new JSONObject(userJson.toString());
                JSONArray whitelistJSON = listData.getJSONArray("whitelist");
                for (Object whiteListItem : whitelistJSON) {
                    whiteList.add((new JSONObject(whiteListItem.toString())).getString("guildID"));
                }

                JSONArray blacklistJSON = listData.getJSONArray("blacklist");
                for (Object blackListItem : blacklistJSON) {
                    blackList.add((new JSONObject(blackListItem.toString())).getString("guildID"));
                }
            }
            response.body().close();

            log(CustomLog.Level.INFO, "Loaded black and whitelist.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public static String insetIntoWhitelist(String guildId, String guildName, String a1234567890) {
        whiteList.add(guildId);
        return insertIntoWhiteOrBlacklist(guildId, guildName, "whiteList", a1234567890);
    }

    public static String insetIntoBlacklist(String guildId, String guildName, String a1234567890) {
        blackList.add(guildId);
        return insertIntoWhiteOrBlacklist(guildId, guildName, "blackList", a1234567890);
    }

    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, MessageReceivedEvent event){
        String length = "";
        if (!time.isEmpty()) { length = " lasting " + time + ""; }
        String punishedUserMention = "<@" + punishedUser.getId() + ">";
        MessageChannel modLogChannel = event.getGuild().getTextChannelsByName("modlog", true).get(0);
        modLogChannel.sendMessage(embedField(punishedUser.getName() + " " + punishment, punishment
                + " by " + mod.getName() + length + (reason.isEmpty()?"":" for " + reason))).queue(
                        msg -> msg.getTextChannel().sendMessage("_Relevant user: " + punishedUserMention + "_").queue()
        );
    }

    public static void modLog(User mod, User punishedUser, String punishment, String reason, MessageReceivedEvent event) {
        modLog(mod, punishedUser, punishment, reason, "", event);
    }

    public static void modLog(User mod, User unbannedUser, String punishment, MessageReceivedEvent event) {
        modLog(mod, unbannedUser, punishment, "", event);
    }

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

    public static void checkUnbans() {
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "delete=true");
            Request request = new Request.Builder()
                    .url(Config.apiBase + "/getUnbans.php")
                    .post(body)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonData = response.body().source().readUtf8();response.body().source().readUtf8();
            JSONArray json = new JSONArray(jsonData);
            for(Object userJson : json) {
                JSONObject userData = new JSONObject(userJson.toString());
                SkyBot.jda.getGuildById(userData.getString("guild"))
                .getController().unban(userData.getString("userId")).reason("Ban expired").queue();
            }
            response.body().close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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

    public static void log(CustomLog.Level lvl, String message){
        log(Config.defaultName, lvl, message);
    }

    public static void log(String name, CustomLog.Level lvl, Object message){
        logger2 = CustomLog.getLog(name);
        logger2.log(lvl, message);
    }
}
