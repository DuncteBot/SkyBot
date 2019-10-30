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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import io.sentry.Sentry;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.adapters.WebDatabaseAdapter;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.MapUtils;
import net.notfab.caching.client.CacheClient;
import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public final class Variables {

    private final ObjectMapper mapper = new ObjectMapper();
    private final PrettyTime prettyTime = new PrettyTime();
    private final String googleBaseUrl;
    private final TLongObjectMap<TLongLongMap> vcAutoRoleCache = MapUtils.newLongObjectMap();
    private AudioUtils audioUtils;
    private Alexflipnote alexflipnote;
    private WeebApi weebApi;
    private CommandManager commandManager;
    private BlargBot blargBot;
    private DunctebotConfig config;
    private final DuncteApis apis;
    private DatabaseAdapter databaseAdapter;
    private CacheClient youtubeCache;
    private final LoadingCache<Long, GuildSettings> guildSettingsCache = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build((guildId) -> {
            final CompletableFuture<GuildSettings> future = new CompletableFuture<>();
            getDatabaseAdapter().loadGuildSetting(guildId, (setting) -> {
                future.complete(setting);
                return null;
            });

            return future.get();
        });


    Variables() {
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        this.mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        try {
            this.config = this.mapper.readValue(new File("config.json"), DunctebotConfig.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (this.config == null) {
            System.exit(0);
        }

        this.apis = new DuncteApis("Bot " + this.config.discord.token, this.mapper);

        //set the devs
        Settings.DEVELOPERS.addAll(this.config.discord.constantSuperUserIds);
        this.googleBaseUrl = "https://www.googleapis.com/customsearch/v1?q=%s&cx=012048784535646064391:v-fxkttbw54" +
            "&hl=en&searchType=image&key=" + this.config.apis.googl + "&safe=off";

        if (config.sentry.enabled) {
            final String env = "&environment=" + (Settings.IS_LOCAL ? "local" : "production");
            Sentry.init(config.sentry.dsn + "?release=" + Settings.VERSION + env);
        }
    }

    public BlargBot getBlargBot() {
        if (this.blargBot == null) {
            this.blargBot = new BlargBot(this.config.apis.blargbot, this.mapper);
        }

        return this.blargBot;
    }

    public CommandManager getCommandManager() {
        if (this.commandManager == null) {
            this.commandManager = new CommandManager(this);
        }

        return this.commandManager;
    }

    public DunctebotConfig getConfig() {
        return config;
    }

    public LoadingCache<Long, GuildSettings> getGuildSettingsCache() {
        return guildSettingsCache;
    }

    /**
     * Returns the vc autorole cache
     * <p>
     * Layout:
     * Guild id ->
     * Voice channel id -> Role id
     *
     * @return The vc autorole cache
     */
    public TLongObjectMap<TLongLongMap> getVcAutoRoleCache() {
        return vcAutoRoleCache;
    }

    public String getGoogleBaseUrl() {
        return this.googleBaseUrl;
    }

    public CacheClient getYoutubeCache() {
        if (this.youtubeCache == null) {
            var cfg = getConfig().apis.youtubeCache;
            this.youtubeCache = new CacheClient(cfg.endpoint, cfg.token, Executors.newCachedThreadPool((r) -> {
                final Thread thread = new Thread(r, "Cache-Thread");
                thread.setDaemon(true);
                return thread;
            }));
        }

        return this.youtubeCache;
    }

    public WeebApi getWeebApi() {
        if (this.weebApi == null) {
            this.weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS)
                .setBotInfo("DuncteBot(SkyBot)", Settings.VERSION, "Production")
                .setToken(this.config.apis.weebSh)
                .build();
        }

        return this.weebApi;
    }

    boolean useApi() {
        return this.config.use_database;
    }

    public Alexflipnote getAlexflipnote() {
        if (this.alexflipnote == null) {
            this.alexflipnote = new Alexflipnote(this.mapper);
        }

        return this.alexflipnote;
    }

    public ObjectMapper getJackson() {
        return this.mapper;
    }

    public AudioUtils getAudioUtils() {
        if (this.audioUtils == null) {
            this.audioUtils = new AudioUtils(this.config.apis, this);
        }

        return this.audioUtils;
    }

    public DuncteApis getApis() {
        return this.apis;
    }

    public DatabaseAdapter getDatabaseAdapter() {
        try {
            if (this.databaseAdapter == null) {
                this.databaseAdapter = this.useApi() ?
                    new WebDatabaseAdapter(this.getApis(), this.getJackson()) :
                    (DatabaseAdapter) (Class.forName("ml.duncte123.skybot.adapters.SqliteDatabaseAdapter")
                        .getDeclaredConstructor().newInstance());
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
            InstantiationException | InvocationTargetException e) {
            LoggerFactory.getLogger(Variables.class).error("Could not load database class.\n" +
                "Are you a developer?", e);
        }

        return this.databaseAdapter;
    }

    public PrettyTime getPrettyTime() {
        return prettyTime;
    }
}
