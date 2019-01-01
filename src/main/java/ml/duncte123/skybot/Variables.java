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

import com.google.common.io.Files;
import com.google.gson.Gson;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.adapters.SqliteDatabaseAdapter;
import ml.duncte123.skybot.adapters.WebDatabaseAdapter;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AudioUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class Variables {

    private final String googleBaseUrl;
    private final boolean isSql;
    private final TLongObjectMap<GuildSettings> guildSettings = new TLongObjectHashMap<>();
    private AudioUtils audioUtils;
    private Alexflipnote alexflipnote;
    private WeebApi weebApi;
    private DBManager database;
    private CommandManager commandManager;
    private BlargBot blargBot;
    private DunctebotConfig config;
    private DuncteApis apis;
    private DatabaseAdapter databaseAdapter;

    private static Variables instance;


    private Variables() {
        try {
            final String json = Files.asCharSource(new File("config.json"), StandardCharsets.UTF_8).read();
            this.config = new Gson().fromJson(json, DunctebotConfig.class);
        } catch (IOException e) {
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

        if (this.databaseAdapter == null) {
            this.databaseAdapter = this.isSql ?
                new WebDatabaseAdapter(this) :
                new SqliteDatabaseAdapter(this);
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
