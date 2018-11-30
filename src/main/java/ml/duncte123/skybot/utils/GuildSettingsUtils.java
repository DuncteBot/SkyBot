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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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
        loadEmbedColors(variables.getDatabase());
    }


    private static void loadGuildSettings(DatabaseAdapter databaseAdapter, TLongObjectMap<GuildSettings> guildSettings) {
        logger.debug("Loading Guild settings.");

        databaseAdapter.getGuildSettings(
            (storedSettings) -> {

                storedSettings.forEach(
                    (setting) -> guildSettings.put(setting.getGuildId(), setting)
                );

                logger.debug("Loaded settings for " + guildSettings.keySet().size() + " guilds.");

                return null;
            }
        );
    }

    private static void loadEmbedColors(DBManager database) {
        logger.debug("Loading embed colors.");
        String dbName = database.getName();

        database.run(() -> {
            try (Connection connection = database.getConnManager().getConnection()) {
                Statement smt = connection.createStatement();

                ResultSet res = smt.executeQuery("SELECT * FROM " + dbName + ".embedSettings");

                int loaded = 0;

                while (res.next()) {
                    long guildId = toLong(res.getString("guild_id"));
                    int color = res.getInt("embed_color");

                    EmbedUtils.addColor(guildId, color);
                    loaded++;
                }

                logger.debug("Loaded embed colors for " + loaded + " guilds.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        TLongObjectMap<GuildSettings> guildSettings = variables.getGuildSettings();
        DBManager database = variables.getDatabase();
        if (!guildSettings.containsKey(settings.getGuildId())) {
            registerNewGuild(guild, variables);
            return;
        }
        database.run(() -> {
            String dbName = database.getName();

            try (Connection connection = database.getConnManager().getConnection()) {
                PreparedStatement smt = connection.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
                    "enableJoinMessage= ? , " +
                    "enableSwearFilter= ? ," +
                    "customWelcomeMessage= ? ," +
                    "prefix= ? ," +
                    "autoRole= ? ," +
                    "logChannelId= ? ," +
                    "welcomeLeaveChannel= ? ," +
                    "customLeaveMessage = ? ," +
                    "serverDesc = ? ," +
                    "announceNextTrack = ? ," +
                    "autoDeHoist = ? ," +
                    "filterInvites = ? ," +
                    "spamFilterState = ? ," +
                    "muteRoleId = ? ," +
                    "ratelimits = ? ," +
                    "kickInsteadState = ? " +
                    "WHERE guildId='" + settings.getGuildId() + "'");
                smt.setBoolean(1, settings.isEnableJoinMessage());
                smt.setBoolean(2, settings.isEnableSwearFilter());
                smt.setString(3, fixUnicodeAndLines(settings.getCustomJoinMessage()));
                smt.setString(4, replaceUnicode(settings.getCustomPrefix()));
                smt.setString(5, String.valueOf(settings.getAutoroleRole()));
                smt.setString(6, String.valueOf(settings.getLogChannel()));
                smt.setString(7, String.valueOf(settings.getWelcomeLeaveChannel()));
                smt.setString(8, fixUnicodeAndLines(settings.getCustomLeaveMessage()));
                smt.setString(9, fixUnicodeAndLines(settings.getServerDesc()));
                smt.setBoolean(10, settings.isAnnounceTracks());
                smt.setBoolean(11, settings.isAutoDeHoist());
                smt.setBoolean(12, settings.isFilterInvites());
                smt.setBoolean(13, settings.isEnableSpamFilter());
                smt.setString(14, String.valueOf(settings.getMuteRoleId()));
                smt.setString(15, convertJ2S(settings.getRatelimits()));
                smt.setBoolean(16, settings.getKickState());
                smt.executeUpdate();

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
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
        DBManager database = variables.getDatabase();
        if (guildSettings.containsKey(g.getIdLong())) {
            return guildSettings.get(g.getIdLong());
        }
        GuildSettings newGuildSettings = new GuildSettings(g.getIdLong());
        database.run(() -> {

            String dbName = database.getName();

            try (Connection connection = database.getConnManager().getConnection()) {
                ResultSet resultSet = connection.createStatement()
                    .executeQuery("SELECT id FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
                int rows = 0;
                while (resultSet.next())
                    rows++;

                if (rows == 0) {
                    PreparedStatement smt = connection.prepareStatement("INSERT INTO " + dbName + ".guildSettings(guildId," +
                        "customWelcomeMessage, prefix, customLeaveMessage, ratelimits) " +
                        "VALUES('" + g.getId() + "' , ? , ? , ? , ?)");
                    smt.setString(1, newGuildSettings.getCustomJoinMessage());
                    smt.setString(2, Settings.PREFIX);
                    smt.setString(3, newGuildSettings.getCustomLeaveMessage().replaceAll("\\P{Print}", ""));
                    smt.setString(4, "20|45|60|120|240|2400".replaceAll("\\P{Print}", ""));
                    smt.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            guildSettings.put(g.getIdLong(), newGuildSettings);
        });
        return newGuildSettings;
    }

    public static void updateEmbedColor(Guild g, int color, Variables variables) {
        DBManager database = variables.getDatabase();

        database.run(() -> {
            String dbName = database.getName();

            try (Connection connection = database.getConnManager().getConnection()) {

                String updateString = "ON DUPLICATE KEY UPDATE embed_color = ?";

                if (!variables.isSql()) {
                    updateString = "ON CONFLICT(guild_id) DO UPDATE SET embed_color = ?";
                }

                PreparedStatement smt = connection.prepareStatement(
                    "INSERT INTO " + dbName + ".embedSettings(guild_id, embed_color) VALUES( ? , ? ) " + updateString);

                smt.setString(1, g.getId());
                smt.setInt(2, color);
                smt.setInt(3, color);

                smt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Database error: ", e);
            }

        });
    }

    /**
     * This will attempt to remove a guild wen we leave it
     *
     * @param g
     *         the guild to remove from the database
     */
    public static void deleteGuild(Guild g, Variables variables) {
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

    private static String replaceUnicode(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\P{Print}", "");
    }

    /*private static String replaceUnicodeAndLines(String s) {
        return replaceUnicode(replaceNewLines(s));
    }*/

    private static String fixUnicodeAndLines(String s) {
        return replaceUnicode(fixNewLines(replaceNewLines(s)));
    }

    private static String convertJ2S(long[] in) {
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
        try {
            return s == 1;
        } catch (Exception ignored) {
            return false;
        }
    }
}
