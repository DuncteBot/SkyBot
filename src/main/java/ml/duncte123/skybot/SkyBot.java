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

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import lavalink.client.io.Lavalink;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

/**
 * NOTE TO SELF String.format("%#s", userObject)
 */
//Skybot version 1.0 and 2.0 where written in php
@SinceSkybot(version = "3.0.0")
@Author
public class SkyBot {

    public static final Logger logger = LoggerFactory.getLogger(SkyBot.class);

    private static ShardManager shardManager = null;
    private static Lavalink lavalink = null;

    private static final SkyBot instance = new SkyBot();

    private SkyBot() {}

    /**
     * This is our main method
     *
     * @param args The args passed in while running the bot
     * @throws Exception When you mess something up
     * @deprecated Because I can lol
     */
    @Deprecated
    public static void main(String... args) throws Exception {

        if (AirUtils.config.hasKey("launch_unstable") && AirUtils.config.getBoolean("launch_unstable", false)) {
            RestAction.DEFAULT_FAILURE = ComparatingUtils::execCheck;
            logger.info(TextColor.RED_BACKGROUND+TextColor.YELLOW+"USING UNSTABLE BUILD!"+TextColor.RESET);
        }

        //Set the logger to only info by default
//        Logger l = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        l.setLevel(INFO);

        //Set the value for other classes to use
        boolean useDatabase = AirUtils.nonsqlite;
        if (useDatabase) { //Don't try to connect if we don't want to
            if (!AirUtils.db.connManager.hasSettings()) {
                logger.error("Can't load database settings. ABORTING!!!!!");
                System.exit(-2);
            }
            if (!AirUtils.db.isConnected()) {
                logger.error("Can't connect to database. ABORTING!!!!!");
                System.exit(-3);
            }
        } else {
            int startIn = 5;
            logger.warn("Using SQLite as the database");
            logger.warn("Please note that is is not recommended and can break some features.");
            logger.warn("Please report bugs on GitHub (https://github.com/duncte123/SkyBot/issues)");
            Thread.sleep(DateUtils.MILLIS_PER_SECOND * startIn);
        }


        //Load the settings before loading the bot
        GuildSettingsUtils.loadAllSettings();

        //Load the tags
        TagUtils.loadAllTags();

        //Set the token to a string
        String token = AirUtils.config.getString("discord.token", "Your Bot Token");

        //But this time we are going to shard it
        int TOTAL_SHARDS = AirUtils.config.getInt("discord.totalShards", 1);

        //Set the game from the config
        int gameId = AirUtils.config.getInt("discord.game.type", 3);
        String name = AirUtils.config.getString("discord.game.name", "over shard #{shardId}");
        String[] url = {"https://www.twitch.tv/duncte123"};


        Game.GameType type = Game.GameType.fromKey(gameId);
        if(type.equals(Game.GameType.STREAMING)) {
            url[0] = AirUtils.config.getString("discord.game.streamUrl", url[0]);
        }

        logger.info(AirUtils.commandManager.getCommands().size() + " commands loaded.");
        lavalink = new Lavalink(
                getIdFromToken(token),
                TOTAL_SHARDS,
                shardId -> SkyBot.getInstance().getShardManager().getShardById(shardId)
        );
        if(AirUtils.config.getBoolean("lavalink.enable", false)) {
            try {
                lavalink.addNode(
                        new URI(AirUtils.config.getString("lavalink.wsurl", "ws://localhost")),
                        AirUtils.config.getString("lavalink.pass", "youshalnotpass")
                );
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        try {
            //Set up sharding for the bot
            shardManager = new DefaultShardManagerBuilder()
                    .setEventManager(new EventManager())
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .setShardsTotal(TOTAL_SHARDS)
                    .setGameProvider(shardId -> Game.of(type,
                            name.replace("{shardId}", Integer.toString(shardId + 1)), url[0])
                    )
                    .setToken(token)
                    .build();
        } catch (LoginException e) {
            //Kill the system if we can't log in
            logger.error(TextColor.RED + "Could not log in, check if your token is correct" + TextColor.RESET, e);
            System.exit(-4);
        }

        //Load all the commands for the help embed last
        HelpEmbeds.init();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public static SkyBot getInstance() {
        return instance;
    }

    public Lavalink getLavalink() {
        return lavalink;
    }

    /**
     * This is a simple util function that extracts the bot id from the token
     * @param token the token of your bot
     * @return the client id of the bot
     */
    private static String getIdFromToken(String token) {

        return new String(
                Base64.getDecoder().decode(
                        token.split("\\.")[0]
                )
        );
    }
}
