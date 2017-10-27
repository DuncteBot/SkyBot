/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

        //we check if we need to use the db first
        boolean useDatabase = AirUtils.config.getBoolean("use_database", false);
        //Set the value for other classes to use
        AirUtils.use_database = useDatabase;
        if(useDatabase) { //Don't try to connect if we don't want to
            if (!AirUtils.db.connManager.hasSettings()) {
                AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "Can't load database settings. ABORTING!!!!!");
                System.exit(-2);
                return;
            }
            if (!AirUtils.db.isConnected()) {
                AirUtils.log(Settings.defaultName + "Main", Level.ERROR, "Can't connect to database. ABORTING!!!!!");
                System.exit(-3);
                return;
            }
        } else {
            int startIn = 5;
            AirUtils.logger.warn("Using SQLite as the database");
            AirUtils.logger.warn("Please note that is is not recommended and can break some features.");
            AirUtils.logger.warn("Please report bugs on GitHub (https://github.com/duncte123/SkyBot/issues)");
            AirUtils.logger.warn("The bot will start in "+startIn+" seconds");
            Thread.sleep(DateUtils.MILLIS_PER_SECOND * startIn);
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
                .buildAsync();

        if(useDatabase) {
            //Register the timer for the auto unbans
            //I moved the timer here to make sure that every running jar has this only once
            TimerTask unbanTask = new TimerTask() {
                @Override
                public void run() {
                    AirUtils.checkUnbans(mgr);
                }
            };
            listener.unbanTimer.schedule(unbanTask, DateUtils.MILLIS_PER_MINUTE * 10, DateUtils.MILLIS_PER_MINUTE * 10);
            listener.unbanTimerRunning = true;

            //This handles the updating from the setting and quotes
            TimerTask settingsTask = new TimerTask() {
                @Override
                public void run() {
                    GuildSettingsUtils.loadAllSettings();
                }
            };
            listener.settingsUpdateTimer.schedule(settingsTask, DateUtils.MILLIS_PER_HOUR, DateUtils.MILLIS_PER_HOUR);
            listener.settingsUpdateTimerRunning = true;
        }
    }
}
