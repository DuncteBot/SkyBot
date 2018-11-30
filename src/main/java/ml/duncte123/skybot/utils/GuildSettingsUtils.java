/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import gnu.trove.map.TLongObjectMap;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);


    public static void loadAllSettings(Variables variables) {
        loadGuildSettings(variables.getDatabaseAdapter(), variables.getGuildSettings());
        loadEmbedColors(variables.getDatabaseAdapter());
    }


    private static void loadGuildSettings(DatabaseAdapter databaseAdapter, TLongObjectMap<GuildSettings> guildSettings) {
        logger.info("Loading Guild settings.");

        databaseAdapter.getGuildSettings(
            (storedSettings) -> {

                storedSettings.forEach(
                    (setting) -> guildSettings.put(setting.getGuildId(), setting)
                );

                logger.info("Loaded settings for " + guildSettings.keySet().size() + " guilds.");

                return null;
            }
        );
    }

    private static void loadEmbedColors(DatabaseAdapter databaseAdapter) {
        logger.info("Loading embed colors.");

        databaseAdapter.loadEmbedSettings(
            (settings) -> {
                int loaded = 0;

                for (long key : settings.keys()) {
                    EmbedUtils.addColor(key, settings.get(key));
                    loaded++;
                }

                logger.info("Loaded embed colors for " + loaded + " guilds.");

                return null;
            }
        );
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild
     *         the guild to get
     *
     * @return the guild
     */
    @NotNull
    public static GuildSettings getGuild(Guild guild, Variables variables) {
        TLongObjectMap<GuildSettings> guildSettings = variables.getGuildSettings();

        if (!guildSettings.containsKey(guild.getIdLong())) {
            return registerNewGuild(guild, variables);
        }

        return guildSettings.get(guild.getIdLong());

    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     *
     * @param guild
     *         The guild to update it for
     * @param settings
     *         the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings, Variables variables) {
        if (!variables.getGuildSettings().containsKey(settings.getGuildId())) {
            registerNewGuild(guild, variables);
            return;
        }

        variables.getDatabaseAdapter().updateGuildSetting(settings, (bool) -> null);
    }

    /**
     * This will register a new guild with their settings on bot join
     *
     * @param g
     *         The guild that we are joining
     *
     * @return The new guild
     */
    public static GuildSettings registerNewGuild(Guild g, Variables variables) {
        TLongObjectMap<GuildSettings> guildSettings = variables.getGuildSettings();

        if (guildSettings.containsKey(g.getIdLong())) {
            return guildSettings.get(g.getIdLong());
        }

        GuildSettings newGuildSettings = new GuildSettings(g.getIdLong());
        variables.getDatabaseAdapter().registerNewGuild(newGuildSettings, (bool) -> null);
        guildSettings.put(g.getIdLong(), newGuildSettings);

        return newGuildSettings;
    }

    public static void updateEmbedColor(Guild g, int color, Variables variables) {
        variables.getDatabaseAdapter().updateOrCreateEmbedColor(g.getIdLong(), color);
    }

    /*
     * This will attempt to remove a guild when we leave it
     *
     * @param g
     *         the guild to remove from the database
     */
    /*public static void deleteGuild(Guild g, Variables variables) {
        TLongObjectMap<GuildSettings> guildSettings = variables.getGuildSettings();
        DBManager database = variables.getDatabase();
        guildSettings.remove(g.getIdLong());
        database.run(() -> {
            String dbName = database.getName();

            try (Connection connection = database.getConnManager().getConnection()) {
                Statement smt = connection.createStatement();
                smt.execute("DELETE FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }*/

    public static String replaceNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\\\n", "\n");
    }

    private static String fixNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\n", "\\\\n");
    }

    public static String replaceUnicode(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\P{Print}", "");
    }

    /*private static String replaceUnicodeAndLines(String s) {
        return replaceUnicode(replaceNewLines(s));
    }*/

    public static String fixUnicodeAndLines(String s) {
        return replaceUnicode(fixNewLines(replaceNewLines(s)));
    }

    public static String convertJ2S(long[] in) {
        return Arrays.stream(in).mapToObj(String::valueOf).collect(Collectors.joining("|", "", ""));
    }

    private static long[] convertS2J(String in) {
        if (in.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};
        return Arrays.stream(in.split("\\|")).mapToLong(Long::valueOf).toArray();
    }

    public static long[] ratelimmitChecks(String fromDb) {
        if (fromDb == null || fromDb.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};

        return convertS2J(fromDb.replaceAll("\\P{Print}", ""));
    }

    public static long toLong(String s) {
        try {
            return Long.parseUnsignedLong(s);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    public static boolean toBool(int s) {
        return s == 1;
    }
}
