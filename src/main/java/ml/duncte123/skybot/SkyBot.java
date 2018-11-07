/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fredboat.audio.player.LavalinkManager;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.text.TextColor;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.HelpEmbeds;
import ml.duncte123.skybot.web.WebHolder;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.Instant;
import java.util.EnumSet;

/**
 * NOTE TO SELF String.format("%#s", userObject)
 */
//Skybot version 1.0 and 2.0 where written in php
@SinceSkybot(version = "3.0.0")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class SkyBot {

    private static SkyBot instance;
    private final ShardManager shardManager;

    private SkyBot() throws Exception {

        Variables variables = new Variables();
        DunctebotConfig config = variables.getConfig();
        DBManager database = variables.getDatabase();
        CommandManager commandManager = variables.getCommandManager();
        Logger logger = LoggerFactory.getLogger(SkyBot.class);

        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://bot.duncte123.me;)");
        EmbedUtils.setEmbedBuilder(
            () -> new EmbedBuilder()
                .setColor(Settings.defaultColour)
                .setFooter(Settings.DEFAULT_NAME, Settings.DEFAULT_ICON)
                .setTimestamp(Instant.now())
        );

        String configPrefix = config.discord.prefix;
        if (!Settings.PREFIX.equals(configPrefix)) {
            Settings.PREFIX = configPrefix;
        }

        //throwable.printStackTrace();
        RestAction.DEFAULT_FAILURE = (t) -> {
        };
        RestAction.setPassContext(true);

        if (variables.isSql()) { //Don't try to connect if we don't want to
            if (!database.getConnManager().hasSettings()) {
                logger.error("Can't load database settings. ABORTING!!!!!");
                System.exit(-2);
            }

            try (Connection conn = database.getConnection()) {

                if (conn.isClosed() && variables.isSql()) {
                    logger.error("Can't connect to database. ABORTING!!!!!");
                    System.exit(-3);
                }

                logger.info(TextColor.GREEN + "Successful connection to the database" + TextColor.RESET);
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
        GuildSettingsUtils.loadAllSettings(variables);

        //Set the token to a string
        String token = config.discord.token;

        //But this time we are going to shard it
        int TOTAL_SHARDS = config.discord.totalShards;

        //Set the game from the config
        int gameId = config.discord.game.type;
        String name = config.discord.game.name;
        String url = "https://www.twitch.tv/duncte123";


        Game.GameType type = Game.GameType.fromKey(gameId);
        if (type.equals(Game.GameType.STREAMING)) {
            url = config.discord.game.streamUrl;
        }

        logger.info(commandManager.getCommands().size() + " commands loaded.");
        LavalinkManager.ins.start(config, variables.getAudioUtils());
        final String finalUrl = url;

        //Set up sharding for the bot
        EventManager eventManager = new EventManager(variables);
        this.shardManager = new DefaultShardManagerBuilder()
            .setEventManagerProvider((id) -> eventManager)
            .setBulkDeleteSplittingEnabled(false)
            .setDisabledCacheFlags(EnumSet.of(CacheFlag.EMOTE, CacheFlag.GAME))
            .setShardsTotal(TOTAL_SHARDS)
            .setGameProvider(shardId -> Game.of(type,
                name.replace("{shardId}", Integer.toString(shardId + 1)), finalUrl)
            )
            .setToken(token)
            .build();

        //Load all the commands for the help embed last
        HelpEmbeds.init(commandManager);

        if (!config.discord.local) {
            // init web server
            new WebHolder(shardManager, variables);
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    /**
     * This is our main method
     *
     * @param args
     *         The args passed in while running the bot
     *
     * @throws Exception
     *         When you mess something up
     * @deprecated Because I can lol
     */
    @Deprecated
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            if ("--gen".equals(arg)) {
                gen();
                return;
            }
        }
        instance = new SkyBot();
    }

    public static SkyBot getInstance() {
        return instance;
    }

    private static void gen() {
        DunctebotConfig config = new DunctebotConfig();

        DunctebotConfig.Discord discord = new DunctebotConfig.Discord();
        discord.local = false;
        DunctebotConfig.Discord.Game game = new DunctebotConfig.Discord.Game();
        game.name = "Danny Phantom on shard #{shardId}";
        game.type = 3;
        discord.game = game;
        discord.botOwnerId = "191231307290771456";
        discord.constantSuperUserIds = new long[]{
            191231307290771456L
        };
        DunctebotConfig.Discord.Oauth oauth = new DunctebotConfig.Discord.Oauth();
        oauth.clientId = 215011992275124225L;
        discord.oauth = oauth;
        config.discord = discord;

        DunctebotConfig.Apis apis = new DunctebotConfig.Apis();

        apis.trello = new DunctebotConfig.Apis.Trello();

        apis.weebSh = new DunctebotConfig.Apis.WeebSh();

        apis.chapta = new DunctebotConfig.Apis.Chapta();

        apis.spotify = new DunctebotConfig.Apis.Spotify();
        config.apis = apis;

        config.genius = new DunctebotConfig.Genius();

        DunctebotConfig.Lavalink lavalink = new DunctebotConfig.Lavalink();
        lavalink.enable = true;
        DunctebotConfig.Lavalink.LavalinkNode node = new DunctebotConfig.Lavalink.LavalinkNode();
        lavalink.nodes = new DunctebotConfig.Lavalink.LavalinkNode[]{node};
        config.lavalink = lavalink;

        config.use_database = true;
        config.sql = new DunctebotConfig.Sql();

        GsonBuilder builder = new Gson().newBuilder().setPrettyPrinting().serializeNulls();
        String json = builder.create().toJson(config);
        try {
            FileUtils.writeStringToFile(new File("config-empty.json"), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
