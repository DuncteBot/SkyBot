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

package ml.duncte123.skybot.utils;

import com.dunctebot.models.settings.GuildSetting;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongLongHashMap;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.database.AbstractDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

public class GuildSettingsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildSettingsUtils.class);

    private GuildSettingsUtils() {}

    public static void loadVcAutoRoles(Variables variables) {
        final AbstractDatabase adapter = variables.getDatabaseAdapter();
        final TLongObjectMap<TLongLongMap> vcAutoRoleCache = variables.getVcAutoRoleCache();

        LOGGER.info("Loading vc auto roles.");

        adapter.getVcAutoRoles().thenAccept((items) -> {
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

            LOGGER.info("Loaded " + items.size() + " vc auto roles.");
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

        variables.getDatabaseAdapter().updateGuildSetting(settings);
    }

    public static GuildSetting registerNewGuild(long guildId, Variables variables) {
        return registerNewGuild(guildId, variables, new GuildSetting(guildId));
    }

    private static GuildSetting registerNewGuild(long guildId, Variables variables, GuildSetting newGuildSettings) {
        final Map<Long, GuildSetting> cache = variables.getGuildSettingsCache();
        final GuildSetting settingForGuild = cache.get(guildId);

        if (settingForGuild != null) {
            return settingForGuild;
        }

        variables.getDatabaseAdapter().registerNewGuild(newGuildSettings);
        variables.getGuildSettingsCache().put(guildId, newGuildSettings);

        return newGuildSettings;
    }

    public static void updateEmbedColor(long guildId, int color, Variables variables) {
        getGuild(guildId, variables).setEmbedColor(color);
        // TODO: save guild setting instead, we've deprecated this
        variables.getDatabaseAdapter().updateOrCreateEmbedColor(guildId, color);
    }
}
