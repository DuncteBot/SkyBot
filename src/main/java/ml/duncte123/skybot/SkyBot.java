/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import fredboat.audio.player.LavalinkManager;
import me.duncte123.botCommons.config.Config;
import me.duncte123.botCommons.text.TextColor;
import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.HelpEmbeds;
import ml.duncte123.skybot.web.WebServer;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * NOTE TO SELF String.format("%#s", userObject)
 */
//Skybot version 1.0 and 2.0 where written in php
@SinceSkybot(version = "3.0.0")
@Author
public class SkyBot {

    private static SkyBot instance;
    private final ShardManager shardManager;

    private SkyBot() throws Exception {

        Variables vars = Variables.ins;
        Config config = vars.getConfig();
        DBManager database = vars.getDatabase();
        CommandManager commandManager = vars.getCommandManager();
        Logger logger = LoggerFactory.getLogger(SkyBot.class);
        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://bot.duncte123.me;)");

        //throwable.printStackTrace();
        RestAction.DEFAULT_FAILURE = (t) -> { };
        RestAction.setPassContext(true);

        if (!vars.isSql()) { //Don't try to connect if we don't want to
            if (!database.connManager.hasSettings()) {
                logger.error("Can't load database settings. ABORTING!!!!!");
                System.exit(-2);
            }
            Connection conn = database.getConnManager().getConnection();
            if (!database.isConnected()) {
                logger.error("Can't connect to database. ABORTING!!!!!");
                System.exit(-3);
            } else {
                logger.info(TextColor.GREEN + "Successful connection to the database" + TextColor.RESET);
                conn.close();
            }
        } else {
            int startIn = 5;
            logger.warn("Using SQLite as the database");
            logger.warn("Please note that is is not recommended and can break some features.");
            logger.warn("Please report bugs on GitHub (https://github.com/duncte123/SkyBot/issues)");
            Thread.sleep(DateUtils.MILLIS_PER_SECOND * startIn);
        }

        //2 seconds safe sleep for database
        Thread.sleep(DateUtils.MILLIS_PER_SECOND * 2);

        //Load the settings before loading the bot
        GuildSettingsUtils.loadAllSettings(database);

        //Set the token to a string
        String token = config.getString("discord.token", "Your Bot Token");

        //But this time we are going to shard it
        int TOTAL_SHARDS = config.getInt("discord.totalShards", 1);

        //Set the game from the config
        int gameId = config.getInt("discord.game.type", 3);
        String name = config.getString("discord.game.name", "over shard #{shardId}");
        String url = "https://www.twitch.tv/duncte123";


        Game.GameType type = Game.GameType.fromKey(gameId);
        if (type.equals(Game.GameType.STREAMING)) {
            url = Variables.ins.getConfig().getString("discord.game.streamUrl", url);
        }

        logger.info(commandManager.getCommands().size() + " commands loaded.");
        LavalinkManager.ins.start(config);
        final String finalUrl = url;

        //Set up sharding for the bot
        EventManager eventManager = new EventManager(vars);
        this.shardManager = new DefaultShardManagerBuilder()
                .setEventManager(eventManager)
                .setShardsTotal(TOTAL_SHARDS)
                .setGameProvider(shardId -> Game.of(type,
                        name.replace("{shardId}", Integer.toString(shardId + 1)), finalUrl)
                )
                .setToken(token)
                .build();

        //Load all the commands for the help embed last
        HelpEmbeds.init(commandManager);

        AudioUtils.ins.setConfig(config);

        if (!config.getBoolean("discord.local", false)) {
            // init web server
            new WebServer(shardManager, config, commandManager, database);
        }
    }

    /**
     * This is our main method
     *
     * @param args The args passed in while running the bot
     * @throws Exception When you mess something up
     * @deprecated Because I can lol
     */
    @Deprecated
    public static void main(String[] args) throws Exception {
        instance = new SkyBot();
    }

    public static SkyBot getInstance() {
        return instance;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
