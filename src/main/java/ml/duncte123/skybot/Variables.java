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

package ml.duncte123.skybot;

import com.dunctebot.models.settings.GuildSetting;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import io.sentry.Sentry;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.database.AbstractDatabase;
import ml.duncte123.skybot.database.PostgreDatabase;
import ml.duncte123.skybot.database.WebDatabase;
import ml.duncte123.skybot.objects.DBMap;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.MapUtils;
import net.jodah.expiringmap.EntryLoader;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.caching.client.CacheClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Variables {
    private final JsonMapper mapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .enable(
            JsonParser.Feature.ALLOW_COMMENTS,
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES
        )
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
    private final String googleBaseUrl;
    private final TLongObjectMap<TLongLongMap> vcAutoRoleCache = MapUtils.newLongObjectMap();
    private final CommandManager commandManager;
    private final DuncteApis apis;
    private final BlargBot blargBot;
    private final AudioUtils audioUtils;
    private final Alexflipnote alexflipnote;
    private final WeebApi weebApi;
    private final DunctebotConfig config;
    private final CacheClient youtubeCache;
    private AbstractDatabase database;
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private final DBMap<Long, GuildSetting> guildSettingsCache = new DBMap<>(ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(12, TimeUnit.HOURS)
        .entryLoader((EntryLoader<Long, GuildSetting>) guildId -> {
            try {
                return getDatabaseAdapter().
                    loadGuildSetting(guildId)
                    .get(20L, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                return null;
            }
        })
        .build());


    /* package */ Variables() {
        this.config = DunctebotConfig.fromEnv();
        this.apis = new DuncteApis("Bot " + this.config.discord.token, this.mapper);
        this.commandManager = new CommandManager(this);
        this.blargBot = new BlargBot(this.config.apis.blargbot, this.mapper);

        // Audio Utils needs the client
        final var ytcfg = this.config.apis.youtubeCache;
        this.youtubeCache = new CacheClient(ytcfg.endpoint, ytcfg.token, Executors.newCachedThreadPool((r) -> {
            final Thread thread = new Thread(r, "Cache-Thread");
            thread.setDaemon(true);

            return thread;
        }));

        this.audioUtils = new AudioUtils(this.config.apis, this);
        this.alexflipnote = new Alexflipnote(this.mapper, this.config.apis.alexflipnote);
        this.weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS)
            .setBotInfo("DuncteBot(SkyBot)", Settings.VERSION, "Production")
            .setToken(this.config.apis.weebSh)
            .build();

        //set the devs
        Settings.DEVELOPERS = this.config.discord.constantSuperUserIds;
        this.googleBaseUrl = "https://www.googleapis.com/customsearch/v1?q=%s&cx=012048784535646064391:v-fxkttbw54" +
            "&hl=en&searchType=image&key=" + this.config.apis.googl + "&safe=off";

        if (config.sentry.enabled) {
            final String env = "&environment=" + (Settings.IS_LOCAL ? "local" : "production");
            Sentry.init(config.sentry.dsn + "?release=" + Settings.VERSION + env);
        }
    }

    public BlargBot getBlargBot() {
        return this.blargBot;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public DunctebotConfig getConfig() {
        return config;
    }

    public DBMap<Long, GuildSetting> getGuildSettingsCache() {
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
        return this.youtubeCache;
    }

    public WeebApi getWeebApi() {
        return this.weebApi;
    }

    public Alexflipnote getAlexflipnote() {
        return this.alexflipnote;
    }

    public JsonMapper getJackson() {
        return this.mapper;
    }

    public AudioUtils getAudioUtils() {
        return this.audioUtils;
    }

    public DuncteApis getApis() {
        return this.apis;
    }

    public AbstractDatabase getDatabaseAdapter() {
        if (this.database == null) {
            if ("psql".equals(this.config.useDatabase)) {
                this.database = new PostgreDatabase();
            } else if ("web".equals(this.config.useDatabase)) {
                this.database = new WebDatabase(this.getApis(), this.getJackson());
            } else {
                throw new IllegalArgumentException("SQLite has been removed");
            }
        }

        return this.database;
    }
}
