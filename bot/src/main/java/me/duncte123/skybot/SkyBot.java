/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot;

import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import fredboat.audio.player.LavalinkManager;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.skybot.objects.config.DunctebotConfig;
import me.duncte123.skybot.objects.pairs.LongLongPair;
import me.duncte123.skybot.utils.GuildSettingsUtils;
import me.duncte123.skybot.utils.HelpEmbeds;
import me.duncte123.skybot.web.WebSocketClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.GatewayEncoding;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import static net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;
import static net.dv8tion.jda.api.utils.MemberCachePolicy.DEFAULT;
import static net.dv8tion.jda.api.utils.MemberCachePolicy.PENDING;

public final class SkyBot {
    public static final IntFunction<Activity> ACTIVITY_PROVIDER = (shardId) -> Activity.playing(
        Settings.PREFIX + "help | Shard " + (shardId + 1)
    );
    public static final ScheduledExecutorService SYSTEM_POOL = Executors.newSingleThreadScheduledExecutor((r) -> {
        final Thread thread = new Thread(r, "System Pool");
        thread.setDaemon(true);
        return thread;
    });

    private static SkyBot instance;
    private static final MemberCachePolicy PATRON_POLICY = (member) -> {
        // Member needs to be cached for JDA to fire role update event
        // the guild check is required for JDA to catch role updates in the support guild
        return member.getGuild().getIdLong() == Settings.SUPPORT_GUILD_ID &&
            member.getRoles().stream().anyMatch((role) -> role.getIdLong() == Settings.PATRONS_ROLE);
    };
    private final ShardManager shardManager;
    private WebSocketClient client;

    private SkyBot() throws LoginException {
        // Load in our container
        final Variables variables = new Variables();

        this.configureDefaults(variables);

        final DunctebotConfig config = variables.getConfig();
        final Logger logger = LoggerFactory.getLogger(SkyBot.class);

        Settings.PREFIX = config.discord.prefix;

        final String useDatabase = variables.getConfig().useDatabase;

        if ("psql".equals(useDatabase)) {
            logger.info("Using PostgreSQL as database impl");
        } else if ("mysql".equals(useDatabase)) {
            logger.warn("Using native MariaDB connection, please migrate to PostgreSQL");
        } else {
            shardManager = null; // for compiling n' stuff
            logger.error("Unknown database driver \"{}\", exiting!", useDatabase);
            return;
        }

        //Load the settings before loading the bot
        GuildSettingsUtils.loadVcAutoRoles(variables);

        //Set the token to a string
        final String token = config.discord.token;
        //But this time we are going to shard it
        final int totalShards = config.discord.totalShards;
        final CommandManager commandManager = variables.getCommandManager();
        final LongLongPair commandCount = commandManager.getCommandCount();

        logger.info("{} commands with {} aliases loaded.", commandCount.getFirst(), commandCount.getSecond());
        LavalinkManager.INS.start(config, variables.getAudioUtils());

        final var jdaVirtualPool = Executors.newVirtualThreadPerTaskExecutor();

        final EventManager eventManager = new EventManager(variables);
        // Build our shard manager
        final DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.create(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES
            )
            .setToken(token)
            .setShardsTotal(totalShards)
            .setActivityProvider(ACTIVITY_PROVIDER)
            .setBulkDeleteSplittingEnabled(false)
            .setEventManagerProvider((id) -> eventManager)
            // Keep guild owners, voice members and patrons in cache
            .setMemberCachePolicy(DEFAULT.or(PENDING).or(PATRON_POLICY))
            // Enable lazy loading
            .setChunkingFilter(ChunkingFilter.NONE)
            // Enable lazy loading for guilds other than our own
            // Not using this because it overrides the member cache policy
            // we're calling loadMembers once the guild is ready
//            .setChunkingFilter((guildId) -> guildId == Settings.SUPPORT_GUILD_ID)
            .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS)
            // Can't enable CLIENT_STATUS because we don't have GatewayIntent.GUILD_PRESENCES
            // (is it worth it to enable it for one command?)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
            .setGatewayEncoding(GatewayEncoding.ETF)
            // Configure JDA to use virtual threads/project loom
            .setCallbackPool(jdaVirtualPool, true)
            .setEventPool(jdaVirtualPool, true)
            .setRateLimitElastic(jdaVirtualPool, true);

        // If lavalink is enabled we will hook it into jda
        if (LavalinkManager.INS.isEnabled()) {
            builder.setVoiceDispatchInterceptor(
                new JDAVoiceUpdateListener(LavalinkManager.INS.getLavalink())
            );
        }

        this.shardManager = builder.build();

        HelpEmbeds.init(commandManager);

        if (config.websocket.enable) {
            client = new WebSocketClient(variables, shardManager);
        }
    }

    private void configureDefaults(Variables variables) {
        // Set our animated emotes as default reactions
        MessageUtils.setErrorReaction("a:_no:577795484060483584");
        MessageUtils.setSuccessReaction("a:_yes:577795293546938369");

        // Set the user-agent of the bot
        WebUtils.setUserAgent("Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://duncte.bot;)");
        EmbedUtils.setDefaultColor(Settings.DEFAULT_COLOUR);
        EmbedUtils.setEmbedBuilder(
            () -> new EmbedBuilder()
                .setColor(Settings.DEFAULT_COLOUR)
//                .setFooter("DuncteBot", Settings.DEFAULT_ICON)
//                .setTimestamp(Instant.now())
        );
        EmbedUtils.setEmbedColorSupplier(
            (guildId) -> GuildSettingsUtils.getGuild(guildId, variables).getEmbedColor()
        );

        MessageRequest.setDefaultMentions(List.of(
            Message.MentionType.USER
            // These two don't get parsed
            // Message.MentionType.CHANNEL,
            // Message.MentionType.EMOTE
        ));
        MessageRequest.setDefaultMentionRepliedUser(false);
        // Set some defaults for rest-actions
        RestAction.setPassContext(true);
        RestAction.setDefaultFailure(ignore(UNKNOWN_MESSAGE));
        // If any rest-action doesn't get executed within 2 minutes we will mark it as failed
        RestAction.setDefaultTimeout(2L, TimeUnit.MINUTES);
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public WebSocketClient getWebsocketClient() {
        return client;
    }

    public static void main(final String[] args) throws LoginException {
        instance = new SkyBot();
    }

    public static SkyBot getInstance() {
        return instance;
    }
}
