package ml.duncte123.skybot;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import ml.duncte123.skybot.logging.CloudListener;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import ml.duncte123.skybot.utils.ResourceUtil;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.event.Level;

/**
 * NOTE TO SELF String.format("%#s", userObject)
 */

public class SkyBot {


    /**
     * This is our main method
     * @param args The args passed in while running the bot
     * @throws Exception When you mess something up
     */
    public static void main(String[] args) throws Exception {
        if(ResourceUtil.getDBProperty("host").isEmpty() ||
                ResourceUtil.getDBProperty("username").isEmpty() ||
                ResourceUtil.getDBProperty("password").isEmpty() ||
                ResourceUtil.getDBProperty("dbname").isEmpty() ) {
            AirUtils.log(Level.ERROR, "DB SETTINGS ARE DOWN ABORTING");
            System.exit(0);
            return;
        }
        if(!AirUtils.db.isConnected()) {
            AirUtils.log(Level.ERROR, "Can't connect to database");
            System.exit(1);
            return;
        }
        //Load the settings before loading the bot
        AirUtils.loadSettings();
        // Register our custom logger and turn the default off
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        SimpleLog.addListener(new CloudListener());

        // log in and set up the api
        /*jda = new JDABuilder(AccountType.BOT)
                .setBulkDeleteSplittingEnabled(false)
                .addEventListener(new BotListener())
                .setToken(Config.token)
                .setGame(Game.of("Use " + Config.prefix + "help"))
                .buildAsync();*/

        //But this time we are going to shard it
        int TOTAL_SHARDS = 1;

        new DefaultShardManagerBuilder()
                .addEventListener(new BotListener())
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setShardTotal(TOTAL_SHARDS)
                .setGame(Game.of("Use " + Config.prefix + "help"))
                .setToken(Config.token)
                .setLoginBackoff(550)
                .buildAsync();
    }
}
