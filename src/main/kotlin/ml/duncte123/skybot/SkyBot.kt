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

@file:JvmName("SkyBot")
@file:SinceSkybot("3.0.0")
@file:Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken"),
    Author(nickname = "ramidzkh", author = "Ramid Khan")
])

package ml.duncte123.skybot

import fredboat.audio.player.LavalinkManager
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.text.TextColor
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.utils.HelpEmbeds
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.bot.sharding.ShardManager
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object SkyBot {

    val shardManager: ShardManager
    private val gameScheduler = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, "Bot-Service-Thread") }
    private val gameProvider: (shard: Int) -> Game

    init {
        MessageUtils.setErrorReaction("a:_no:577795484060483584")
        MessageUtils.setSuccessReaction("a:_yes:577795293546938369")

        val variables = Variables()
        val config = variables.config
        val commandManager = variables.commandManager
        val logger = LoggerFactory.getLogger(SkyBot::class.java)

        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://dunctebot.com;)")
        EmbedUtils.setEmbedBuilder {
            EmbedBuilder()
                .setColor(Settings.defaultColour)
                .setFooter("DuncteBot", Settings.DEFAULT_ICON)
                .setTimestamp(Instant.now())
        }

        Settings.PREFIX = config.discord.prefix
        RestAction.setPassContext(true)

        if (variables.useApi()) {
            logger.info(TextColor.GREEN + "Using api for all connections" + TextColor.RESET)
        } else {
            logger.warn("Using SQLite as the database")
            logger.warn("Please note that is is not recommended for production")
        }

        //Load the settings before loading the bot
        GuildSettingsUtils.loadAllSettings(variables)

        //Set the token to a string
        val token = config.discord.token

        //But this time we are going to shard it
        val totalShards = config.discord.totalShards

        //Set the game from the config
        val gameId = config.discord.game.type
        val name = config.discord.game.name
        val gameType = Game.GameType.fromKey(gameId)
        val streamUrl = if (gameType == Game.GameType.STREAMING) config.discord.game.streamUrl else null

        this.gameProvider = {
            Game.of(
                gameType,
                name.replace("{shardId}", (it + 1).toString()),
                streamUrl
            )
        }

        logger.info("{} commands with {} aliases loaded.", commandManager.commandsMap.size, commandManager.aliasesMap.size)
        LavalinkManager.start(config, variables.audioUtils)


        //Set up sharding for the bot
        val eventManager = EventManager(variables)
        this.shardManager = DefaultShardManagerBuilder()
            .setToken(token)
            .setShardsTotal(totalShards)
            .setGameProvider(this.gameProvider)
            .setBulkDeleteSplittingEnabled(false)
            .setEventManagerProvider { eventManager }
            .setDisabledCacheFlags(EnumSet.of(CacheFlag.GAME))
            .setHttpClientBuilder(
                OkHttpClient.Builder()
                    .connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(30L, TimeUnit.SECONDS)
                    .writeTimeout(30L, TimeUnit.SECONDS)
            )
            .build()

        this.gameScheduler.scheduleAtFixedRate(
            { this.shardManager.setGameProvider(this.gameProvider) },
            1, 1, TimeUnit.DAYS)

        //Load all the commands for the help embed last
        HelpEmbeds.init(commandManager)

        if (!config.discord.local) {
            // init web server
            WebRouter(shardManager, variables)
        }

        // Check shard activity
        ShardWatcher()
    }

    @JvmStatic
    fun main(args: Array<String>) {}
}
