/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import me.duncte123.botcommons.text.TextColor;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.pairs.LongLongPair;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.HelpEmbeds;
import ml.duncte123.skybot.web.WebRouter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.EnumSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

//Skybot version 1.0 and 2.0 were written in php
@SinceSkybot(version = "3.0.0")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public final class SkyBot {

    private static SkyBot instance;
    private final ShardManager shardManager;
    private final ScheduledExecutorService gameScheduler = Executors.newSingleThreadScheduledExecutor((r) -> {
        final Thread thread = new Thread(r, "Game-Update-Thread");
        thread.setDaemon(true);
        return thread;
    });
    private final IntFunction<? extends Activity> activityProvider;
    private WebRouter webRouter = null;

    private SkyBot() throws Exception {
        MessageUtils.setErrorReaction("a:_no:577795484060483584");
        MessageUtils.setSuccessReaction("a:_yes:577795293546938369");

        final Variables variables = new Variables();
        final DunctebotConfig config = variables.getConfig();
        final CommandManager commandManager = variables.getCommandManager();
        final Logger logger = LoggerFactory.getLogger(SkyBot.class);

        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://dunctebot.com;)");
        EmbedUtils.setEmbedBuilder(
            () -> new EmbedBuilder()
                .setColor(Settings.DEFAULT_COLOUR)
//                .setFooter("DuncteBot", Settings.DEFAULT_ICON)
                .setTimestamp(Instant.now())
        );

        Settings.PREFIX = config.discord.prefix;
        RestAction.setPassContext(true);
        RestAction.setDefaultFailure(ErrorResponseException.ignore(EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE)));

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

        this.activityProvider = (shardId) -> Activity.playing(
            config.discord.prefix + "help | Shard " + (shardId + 1)
        );

        final LongLongPair commandCount = commandManager.getCommandCount();

        logger.info("{} commands with {} aliases loaded.", commandCount.getFirst(), commandCount.getSecond());
        LavalinkManager.ins.start(config, variables.getAudioUtils());

        //Set up sharding for the bot
        final EventManager eventManager = new EventManager(variables);
        final DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder()
            .setToken(token)
            .setShardsTotal(totalShards)
            .setActivityProvider(this.activityProvider)
            .setBulkDeleteSplittingEnabled(false)
            .setEventManagerProvider((id) -> eventManager)
            .setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS))
            .setHttpClientBuilder(
                new OkHttpClient.Builder()
                    .connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(30L, TimeUnit.SECONDS)
                    .writeTimeout(30L, TimeUnit.SECONDS)
            );

        this.startGameTimer();

        if (LavalinkManager.ins.isEnabled()) {
            builder.setVoiceDispatchInterceptor(LavalinkManager.ins.getLavalink().getVoiceInterceptor());
        }

        this.shardManager = builder.build();

        HelpEmbeds.init(commandManager);

        if (!config.discord.local) {
            // init web server
            webRouter = new WebRouter(shardManager, variables);
        }
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    private void startGameTimer() {
        this.gameScheduler.scheduleAtFixedRate(
            () -> this.shardManager.setActivityProvider(this.activityProvider),
            1, 1, TimeUnit.DAYS);
    }

    public static void main(final String[] args) throws Exception {
        /*assert AirUtils.isURL("") == false;
        assert AirUtils.isURL("https://google.com/hello") == true;
        assert AirUtils.isURL("https://google.com/ bla bla") == false;
        assert AirUtils.isURL("https://goo gle.com/ bla bla") == false;
        assert AirUtils.isURL("https://google.com/mycool-site") == true;
        assert AirUtils.isURL("google.com") == false;*/

        instance = new SkyBot();
    }

    public static SkyBot getInstance() {
        return instance;
    }

    public WebRouter getWebRouter() {
        return webRouter;
    }
}
