package ml.duncte123.skybot;

import ch.qos.logback.classic.Logger;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.TimerTask;

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
    public static void main(String... args) throws Exception {
        //Set the logger to only info by default
        Logger l = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        l.setLevel(ch.qos.logback.classic.Level.INFO);
        if(!AirUtils.db.connManager.hasSettings()) {
            AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "Can't load database settings. ABORTING!!!!!");
            System.exit(-2);
            return;
        }
        if(!AirUtils.db.isConnected()) {
            AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "Can't connect to database. ABORTING!!!!!");
            System.exit(-3);
            return;
        }

        //Load the settings before loading the bot
        GuildSettingsUtils.loadAllSettings();

        //Set the token to a string
        String token = AirUtils.config.getString("discord.token", "Your Bot Token");

        //But this time we are going to shard it
        int TOTAL_SHARDS = AirUtils.config.getInt("discord.totalShards", 1);

        //Set up the listener in an variable
        BotListener listener = new BotListener();

        //Set up sharding for the bot
        ShardManager mgr = new DefaultShardManagerBuilder()
                .addEventListener(listener) //event.getJDA().getRegisteredListeners().get(0)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setShardTotal(TOTAL_SHARDS)
                .setGame(Game.of("Use " + Settings.prefix + "help"))
                .setToken(token)
                .setLoginBackoff(550)
                .buildAsync();

        //Register the timer for the auto unbans
        //I moved the timer here to make sure that every running jar has this only once
        TimerTask unbanTask = new TimerTask() {
            @Override
            public void run() {
                AirUtils.checkUnbans(mgr);
            }
        };
        listener.unbanTimer.schedule(unbanTask, DateUtils.MILLIS_PER_MINUTE*10, DateUtils.MILLIS_PER_MINUTE*10);
    }
}
