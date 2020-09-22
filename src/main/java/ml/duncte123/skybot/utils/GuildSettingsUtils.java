/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import com.dunctebot.models.settings.GuildSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);

    public static void loadAllSettings(Variables variables) {
        loadGuildSettings(variables.getDatabaseAdapter(), variables.getGuildSettingsCache());
        loadVcAutoRoles(variables.getDatabaseAdapter(), variables.getVcAutoRoleCache());
    }

    private static void loadGuildSettings(DatabaseAdapter databaseAdapter, LoadingCache<Long, GuildSetting> guildSettings) {
        logger.info("Loading Guild settings.");

        databaseAdapter.getGuildSettings(
            (storedSettings) -> {

                storedSettings.forEach(
                    (setting) -> {
                        guildSettings.put(setting.getGuildId(), setting);

                        if (setting.getEmbedColor() != Settings.DEFAULT_COLOUR) {
                            EmbedUtils.addColor(setting.getGuildId(), setting.getEmbedColor());
                        }
                    }
                );

                logger.info("Loaded settings for " + guildSettings.estimatedSize() + " guilds.");

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

    @Nonnull
    public static GuildSetting getGuild(long guildId, Variables variables) {
        final GuildSetting setting = variables.getGuildSettingsCache().get(guildId);

        if (setting == null) {
            return registerNewGuild(guildId, variables);
        }

        return setting;
    }

    public static void updateGuildSettings(long guildId, GuildSetting settings, Variables variables) {
        if (variables.getGuildSettingsCache().get(settings.getGuildId()) == null) {
            registerNewGuild(guildId, variables, settings);
            return;
        }

        variables.getDatabaseAdapter().updateGuildSetting(settings, (bool) -> null);
    }

    public static GuildSetting registerNewGuild(long guildId, Variables variables) {
        return registerNewGuild(guildId, variables, new GuildSetting(guildId));
    }

    private static GuildSetting registerNewGuild(long guildId, Variables variables, GuildSetting newGuildSettings) {
        final LoadingCache<Long, GuildSetting> guildSettingsCache = variables.getGuildSettingsCache();
        final GuildSetting settingForGuild = guildSettingsCache.get(guildId);

        if (settingForGuild != null) {
            return settingForGuild;
        }

        variables.getDatabaseAdapter().registerNewGuild(newGuildSettings, (bool) -> null);
        variables.getGuildSettingsCache().put(guildId, newGuildSettings);

        return newGuildSettings;
    }

    public static void updateEmbedColor(long guildId, int color, Variables variables) {
        variables.getDatabaseAdapter().updateOrCreateEmbedColor(guildId, color);
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
