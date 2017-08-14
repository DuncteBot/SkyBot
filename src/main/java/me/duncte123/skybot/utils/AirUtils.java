package me.duncte123.skybot.utils;

import me.duncte123.skybot.SkyBot;
import net.dv8tion.jda.core.EmbedBuilder;
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

    public static MessageEmbed embedMessage(String message) {
        return defaultEmbed().setDescription(message).build();
    }

    public static MessageEmbed embedField(String title, String message){
        return defaultEmbed().addField(title, message, false).build();
    }

    public static MessageEmbed embedImage(String imageURL) {
        return defaultEmbed().setImage(imageURL).build();
    }

    public static EmbedBuilder defaultEmbed(){
        return new EmbedBuilder()
                .setColor(Config.defaultColour)
                .setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
    }

    public static void getWhiteAndBlackList(){
        try {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType, "delete=true");
            Request request = new Request.Builder()
                    .url("https://bot.duncte123.ml/getWhiteAndBlacklist.php")
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
                    .url("https://bot.duncte123.ml/updateWhiteAndBlacklist.php")
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
        return insertIntoWhiteOrBlacklist(guildId, guildName, "whiteList", a1234567890);
    }

    public static String insetIntoBlacklist(String guildId, String guildName, String a1234567890) {
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
                    .url("https://bot.duncte123.ml/ban.php")
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
                    .url("https://bot.duncte123.ml/getUnbans.php")
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
}
