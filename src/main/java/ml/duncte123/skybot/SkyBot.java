package ml.duncte123.skybot;

import ch.qos.logback.classic.Logger;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.db.DataBaseUtil;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.entities.Game;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * NOTE TO SELF String.format("%#s", userObject)
 */

public class SkyBot {


    /**
     * This is our main method
     * @param args The args passed in while running the bot
     * @throws Exception When you mess something up
     * @deprecated Because I can lol
     */
    @Deprecated
    public static void main(String[] args) throws Exception {
        //Set the logger to only info by default
        Logger l = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        l.setLevel(ch.qos.logback.classic.Level.INFO);

        if(!DataBaseUtil.hasSettings()) {
            AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "DB SETTINGS ARE DOWN ABORTING");
            System.exit(-2);
            return;
        }
        if(!AirUtils.db.isConnected()) {
            AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "Can't connect to database");
            System.exit(-3);
            return;
        }

        //Set database to UTF-8
        AirUtils.db.getConnection().createStatement().execute("SET CHARACTER SET utf8");

        //Load the settings before loading the bot
        GuildSettingsUtils.loadSettings();

        //Set the token to a string
        String token = AirUtils.config.getString("discord.token", "Your Bot Token");

        // log in and set up the api
        //Still need to remove this
        /*jda = new JDABuilder(AccountType.BOT)
                .setBulkDeleteSplittingEnabled(false)
                .addEventListener(new BotListener())
                .setToken(token)
                .setGame(Game.of("Use " + Settings.prefix + "help"))
                .buildAsync();*/

        //But this time we are going to shard it
        int TOTAL_SHARDS = AirUtils.config.getInt("discord.totalShards", 2);

        new DefaultShardManagerBuilder()
                .addEventListener(new BotListener())
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setShardTotal(TOTAL_SHARDS)
                .setGame(Game.of("Use " + Settings.prefix + "help"))
                .setToken(token)
                .setLoginBackoff(550)
                .buildAsync();
    }
}
