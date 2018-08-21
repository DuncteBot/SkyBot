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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.wolfram.alpha.WAEngine;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AudioUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static ml.duncte123.skybot.utils.AirUtils.getWolframEngine;

public class Variables {

    public static final Variables ins = new Variables();
    private final AudioUtils audioUtils;
    private final Alexflipnote alexflipnote;
    private final WAEngine alphaEngine;
    private final String googleBaseUrl;
    private final WeebApi weebApi;
    private final boolean isSql;
    private final ThreadLocalRandom random;
    private final DBManager database;
    private final CommandManager commandManager;
    private final BlargBot blargBot;
    private final Map<Long, GuildSettings> guildSettings;
    private DunctebotConfig config;


    private Variables() {
        try {
            this.config = new Gson().fromJson(Files.asCharSource(new File("config.json"), Charsets.UTF_8).read(), DunctebotConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (config == null) {
            System.exit(0);
        }

        this.audioUtils = new AudioUtils(config.apis);
        this.alphaEngine = getWolframEngine(config.apis.wolframalpha);
        this.googleBaseUrl = "https://www.googleapis.com/customsearch/v1?q=%s&cx=012048784535646064391:v-fxkttbw54" +
                "&hl=en&searchType=image&key=" + config.apis.googl + "&safe=off";
        this.weebApi = new WeebApiBuilder(TokenType.WOLKETOKENS)
                .setBotInfo("DuncteBot(SkyBot)", Settings.VERSION, "Production")
                .setToken(config.apis.weebSh.wolketoken)
                .build();
        this.isSql = config.use_database;
        this.random = ThreadLocalRandom.current();
        this.database = new DBManager(isSql, config.sql);
        this.commandManager = new CommandManager(this);
        this.blargBot = new BlargBot(config.apis.blargbot);
        this.guildSettings = new HashMap<>();
        this.alexflipnote = new Alexflipnote();
    }

    public BlargBot getBlargBot() {
        return blargBot;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DunctebotConfig getConfig() {
        return config;
    }

    public DBManager getDatabase() {
        return database;
    }

    public Map<Long, GuildSettings> getGuildSettings() {
        return guildSettings;
    }

    public ThreadLocalRandom getRandom() {
        return random;
    }

    public String getGoogleBaseUrl() {
        return googleBaseUrl;
    }

    public WAEngine getAlphaEngine() {
        return alphaEngine;
    }

    public WeebApi getWeebApi() {
        return weebApi;
    }

    boolean isSql() {
        return isSql;
    }

    public Alexflipnote getAlexflipnote() {
        return alexflipnote;
    }

    public AudioUtils getAudioUtils() {
        return audioUtils;
    }
}
