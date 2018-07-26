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

package ml.duncte123.skybot.utils;

import com.wolfram.alpha.WAEngine;
import me.duncte123.botCommons.config.Config;
import me.duncte123.weebJava.WeebApiBuilder;
import me.duncte123.weebJava.models.WeebApi;
import me.duncte123.weebJava.types.TokenType;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.web.WebServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ml.duncte123.skybot.utils.AirUtils.getWolframEngine;

public class Variables {
    public static final Config CONFIG = new ConfigUtils().loadConfig();
    public static final WAEngine ALPHA_ENGINE = getWolframEngine();
    public static final String GOOGLE_BASE_URL = "https://www.googleapis.com/customsearch/v1?q=%s&cx=012048784535646064391:v-fxkttbw54" +
            "&hl=en&searchType=image&key=" + CONFIG.getString("apis.googl") + "&safe=off";
    public static final WeebApi WEEB_API = new WeebApiBuilder(TokenType.WOLKETOKENS)
            .setBotInfo("DuncteBot(SkyBot)", Settings.VERSION, "Production")
            .setToken(CONFIG.getString("apis.weeb\\.sh.wolketoken", "INSERT_WEEB_WOLKETOKEN"))
            .build();
    public static final boolean NONE_SQLITE = CONFIG.getBoolean("use_database", false);
    public static final Random RAND = new Random();
    public static final DBManager DATABASE = new DBManager();
    public static final CommandManager COMMAND_MANAGER = new CommandManager();
    static Map<String, GuildSettings> GUILD_SETTINGS = new HashMap<>();
    public static final BlargBot BLARG_BOT = new BlargBot(CONFIG.getString("apis.blargbot", "aaaaa"));

    static {
        if (!CONFIG.getBoolean("discord.local", false)) {
            // init web server
            new WebServer();
        }
    }
}
