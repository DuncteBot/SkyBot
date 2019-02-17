/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.HelpEmbeds;
import ml.duncte123.skybot.web.WebRouter;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.cache.CacheFlag;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

//Skybot version 1.0 and 2.0 where written in php
@SinceSkybot(version = "3.0.0")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public final class SkyBot {

    private static SkyBot instance;
    private final ShardManager shardManager;
    private final ScheduledExecutorService gameScheduler = Executors.newSingleThreadScheduledExecutor(
        (r) -> new Thread(r, "Bot-Service-Thread")
    );
    private final IntFunction<? extends Game> gameProvider;

    private SkyBot() throws Exception {

        final Variables variables = Variables.getInstance();
        final DunctebotConfig config = variables.getConfig();
        final CommandManager commandManager = variables.getCommandManager();
        final Logger logger = LoggerFactory.getLogger(SkyBot.class);

        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://bot.duncte123.me;)");
        EmbedUtils.setEmbedBuilder(
            () -> new EmbedBuilder()
                .setColor(Settings.defaultColour)
                .setFooter(Settings.DEFAULT_NAME, Settings.DEFAULT_ICON)
                .setTimestamp(Instant.now())
        );

        final String configPrefix = config.discord.prefix;
        if (!Settings.PREFIX.equals(configPrefix)) {
            Settings.PREFIX = configPrefix;
        }

        RestAction.setPassContext(true);

        if (variables.useApi()) {
            logger.info(TextColor.GREEN + "Using api for all connections" + TextColor.RESET);
        } else {
            logger.warn("Using SQLite as the database");
            logger.warn("Please note that is is not recommended for production");
        }

        //Load the settings before loading the bot
        GuildSettingsUtils.loadAllSettings(variables);

        //Set the token to a string
        final String token = config.discord.token;

        //But this time we are going to shard it
        final int totalShards = config.discord.totalShards;

        //Set the game from the config
        final int gameId = config.discord.game.type;
        final String name = config.discord.game.name;
        final GameType gameType = GameType.fromKey(gameId);
        final String streamUrl = gameType == GameType.STREAMING ? config.discord.game.streamUrl : null;

        this.gameProvider = (shardId) -> Game.of(
            gameType,
            name.replace("{shardId}", Integer.toString(shardId + 1)),
            streamUrl
        );

        logger.info(commandManager.getCommands().size() + " commands loaded.");
        LavalinkManager.ins.start(config, variables.getAudioUtils());


        //Set up sharding for the bot
        final EventManager eventManager = new EventManager();
        this.shardManager = new DefaultShardManagerBuilder()
            .setToken(token)
            .setShardsTotal(totalShards)
            .setGameProvider(this.gameProvider)
            .setBulkDeleteSplittingEnabled(false)
            .setEventManagerProvider((id) -> eventManager)
            .setDisabledCacheFlags(EnumSet.of(CacheFlag.GAME))
            .build();

        this.startGameTimer();

        //Load all the commands for the help embed last
        HelpEmbeds.init(commandManager);

        if (!config.discord.local) {
            // init web server
            new WebRouter(shardManager);
        }

        // Check shard activity
        new ShardWatcher(this);
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    private void startGameTimer() {
        this.gameScheduler.scheduleAtFixedRate(
            () -> this.shardManager.setGameProvider(this.gameProvider),
            1, 1, TimeUnit.DAYS);
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
    public static void main(final String[] args) throws Exception {
        for (final String arg : args) {
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
        final DunctebotConfig config = new DunctebotConfig();

        final DunctebotConfig.Discord discord = new DunctebotConfig.Discord();
        discord.local = false;
        final DunctebotConfig.Discord.Game game = new DunctebotConfig.Discord.Game();
        game.name = "Danny Phantom on shard #{shardId}";
        game.type = 3;
        discord.game = game;
        discord.botOwnerId = "191231307290771456";
        discord.constantSuperUserIds = new long[]{
            191231307290771456L
        };
        final DunctebotConfig.Discord.Oauth oauth = new DunctebotConfig.Discord.Oauth();
        oauth.clientId = 215011992275124225L;
        discord.oauth = oauth;
        config.discord = discord;

        final DunctebotConfig.Apis apis = new DunctebotConfig.Apis();

        apis.trello = new DunctebotConfig.Apis.Trello();

        apis.weebSh = new DunctebotConfig.Apis.WeebSh();

        apis.chapta = new DunctebotConfig.Apis.Chapta();

        apis.spotify = new DunctebotConfig.Apis.Spotify();
        config.apis = apis;

        config.genius = new DunctebotConfig.Genius();

        final DunctebotConfig.Lavalink lavalink = new DunctebotConfig.Lavalink();
        lavalink.enable = true;
        final DunctebotConfig.Lavalink.LavalinkNode node = new DunctebotConfig.Lavalink.LavalinkNode();
        lavalink.nodes = new DunctebotConfig.Lavalink.LavalinkNode[]{node};
        config.lavalink = lavalink;

        config.use_database = true;
        config.sentry = new DunctebotConfig.Sentry();

        final GsonBuilder builder = new Gson().newBuilder().setPrettyPrinting().serializeNulls();
        final String json = builder.create().toJson(config);
        try {
            FileUtils.writeStringToFile(new File("config-empty.json"), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
