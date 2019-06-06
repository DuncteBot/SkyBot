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

package ml.duncte123.skybot.utils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongLongHashMap;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);


    public static void loadAllSettings(Variables variables) {
        loadGuildSettings(variables.getDatabaseAdapter(), variables.getGuildSettingsCache());
        loadEmbedColors(variables.getDatabaseAdapter());
        loadVcAutoRoles(variables.getDatabaseAdapter(), variables.getVcAutoRoleCache());
    }


    private static void loadGuildSettings(DatabaseAdapter databaseAdapter, LoadingCache<Long, GuildSettings> guildSettings) {
        logger.info("Loading Guild settings.");

        databaseAdapter.getGuildSettings(
            (storedSettings) -> {

                storedSettings.forEach(
                    (setting) -> guildSettings.put(setting.getGuildId(), setting)
                );

                logger.info("Loaded settings for " + guildSettings.estimatedSize() + " guilds.");

                return null;
            }
        );
    }

    private static void loadEmbedColors(DatabaseAdapter databaseAdapter) {
        logger.info("Loading embed colors.");

        databaseAdapter.loadEmbedSettings(
            (settings) -> {
                final AtomicInteger loaded = new AtomicInteger();

                settings.forEachEntry((key, value) -> {
                    EmbedUtils.addColor(key, value);
                    loaded.incrementAndGet();

                    return true;
                });

                logger.info("Loaded embed colors for " + loaded.get() + " guilds.");

                return null;
            }
        );
    }

    private static void loadVcAutoRoles(DatabaseAdapter adapter, TLongObjectMap<TLongLongMap> vcAutoRoleCache) {
        logger.info("Loading vc auto roles.");

        adapter.getVcAutoRoles((items) -> {

            items.forEach(
                (item) -> {
                    final TLongLongMap cache = Optional.ofNullable(
                        vcAutoRoleCache.get(item.getGuildId())
                    )
                        .orElseGet(
                            () -> {
                                vcAutoRoleCache.put(item.getGuildId(), new TLongLongHashMap()); // This returns the old value which was null
                                return vcAutoRoleCache.get(item.getGuildId());
                            }
                        );

                    cache.put(item.getVoiceChannelId(), item.getRoleId());
                }
            );

            logger.info("Loaded " + items.size() + " vc auto roles.");

            return null;
        });
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild
     *         the guild to get
     *
     * @return the guild
     */
    @Nonnull
    public static GuildSettings getGuild(Guild guild, Variables variables) {
        final GuildSettings setting = variables.getGuildSettingsCache().get(guild.getIdLong());

        if (setting == null) {
            return registerNewGuild(guild, variables);
        }

        return setting;

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
        if (variables.getGuildSettingsCache().get(settings.getGuildId()) == null) {
            registerNewGuild(guild, variables, settings);
            return;
        }

        variables.getDatabaseAdapter().updateGuildSetting(settings, (bool) -> null);
    }

    public static GuildSettings registerNewGuild(Guild g, Variables variables) {
        return registerNewGuild(g, variables, new GuildSettings(g.getIdLong()));
    }

    public static GuildSettings registerNewGuild(Guild g, Variables variables, GuildSettings newGuildSettings) {
        final LoadingCache<Long, GuildSettings> guildSettingsCache = variables.getGuildSettingsCache();
        final GuildSettings settingForGuild = guildSettingsCache.get(g.getIdLong());

        if (settingForGuild != null) {
            return settingForGuild;
        }

        variables.getDatabaseAdapter().registerNewGuild(newGuildSettings, (bool) -> null);
        variables.getGuildSettingsCache().put(g.getIdLong(), newGuildSettings);

        return newGuildSettings;
    }

    public static void updateEmbedColor(Guild g, int color, Variables variables) {
        variables.getDatabaseAdapter().updateOrCreateEmbedColor(g.getIdLong(), color);
    }

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

    public static long toLong(@Nullable String s) {
        if (s == null) {
            return 0L;
        }

        try {
            return Long.parseUnsignedLong(s);
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
