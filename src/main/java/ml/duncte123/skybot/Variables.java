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

import com.fasterxml.jackson.databind.ObjectMapper;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import io.sentry.Sentry;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.adapters.WebDatabaseAdapter;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public final class Variables {

    private static Variables instance;
    private final String googleBaseUrl;
    private final boolean isSql;
    private final TLongObjectMap<GuildSettings> guildSettings = MiscUtil.newLongMap();
    private final TLongObjectMap<TLongLongMap> vcAutoRoleCache = MiscUtil.newLongMap();
    private AudioUtils audioUtils;
    private Alexflipnote alexflipnote;
    private WeebApi weebApi;
    private DBManager database;
    private CommandManager commandManager;
    private BlargBot blargBot;
    private DunctebotConfig config;
    private DuncteApis apis;
    private DatabaseAdapter databaseAdapter;


    private Variables() {
        try {
            this.config = new ObjectMapper().readValue(new File("config.json"), DunctebotConfig.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (this.config == null) {
            System.exit(0);
        }

        //set the devs
        Settings.developers.addAll(this.config.discord.constantSuperUserIds);
        this.googleBaseUrl = "https://www.googleapis.com/customsearch/v1?q=%s&cx=012048784535646064391:v-fxkttbw54" +
            "&hl=en&searchType=image&key=" + this.config.apis.googl + "&safe=off";
        this.isSql = this.config.use_database;

        if (config.sentry.enabled) {
            Sentry.init(config.sentry.dsn + "?release=" + Settings.VERSION);
        }
    }

    public BlargBot getBlargBot() {

        if (this.blargBot == null) {
            this.blargBot = new BlargBot(this.config.apis.blargbot);
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

    public DBManager getDatabase() {

        if (this.database == null) {
            this.database = new DBManager(this.useApi());
        }

        return this.database;
    }

    public TLongObjectMap<GuildSettings> getGuildSettings() {
        return this.guildSettings;
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

    public WeebApi getWeebApi() {

        if (this.weebApi == null) {
            this.weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS)
                .setBotInfo("DuncteBot(SkyBot)", Settings.VERSION, "Production")
                .setToken(this.config.apis.weebSh.wolketoken)
                .build();
        }

        return this.weebApi;
    }

    boolean useApi() {
        return this.isSql;
    }

    public Alexflipnote getAlexflipnote() {

        if (this.alexflipnote == null) {
            this.alexflipnote = new Alexflipnote();
        }

        return this.alexflipnote;
    }

    public AudioUtils getAudioUtils() {

        if (this.audioUtils == null) {
            this.audioUtils = new AudioUtils(this.config.apis, this);
        }

        return this.audioUtils;
    }

    public DuncteApis getApis() {

        if (this.apis == null) {
            this.apis = new DuncteApis("Bot " + this.config.discord.token);
        }

        return this.apis;
    }

    public DatabaseAdapter getDatabaseAdapter() {

        try {
            if (this.databaseAdapter == null) {
                this.databaseAdapter = this.isSql ?
                    new WebDatabaseAdapter(this) :
                    (DatabaseAdapter) (Class.forName("ml.duncte123.skybot.adapters.SqliteDatabaseAdapter")
                        .getDeclaredConstructor(Variables.class).newInstance(this));
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
            InstantiationException | InvocationTargetException e) {
            LoggerFactory.getLogger(Variables.class).error("Could not load database class.\n" +
                "Are you a developer?", e);
        }

        return this.databaseAdapter;
    }

    public static synchronized Variables getInstance() {

        if (instance == null) {
            instance = new Variables();
        }

        return instance;
    }
}
