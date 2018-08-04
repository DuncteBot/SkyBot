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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);

    /**
     * This runs both {@link #loadGuildSettings()} and { #loadFooterQuotes()}
     */
    public static void loadAllSettings() {
        loadGuildSettings();
        //loadFooterQuotes();
    }

    /*
     * This will load all the footer quotes from the database and store them in the {@link  EmbedUtils#footerQuotes}
     */
    /*private static void loadFooterQuotes() {
        if (!AirUtils.NONE_SQLITE) return;
        logger.debug("Loading footer quotes");

        String dbName = AirUtils.DATABASE.getName();
        AirUtils.DATABASE.run(() -> {
            Connection database = AirUtils.DATABASE.getConnManager().getConnection();
            try {
                Statement smt = database.createStatement();

                ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".footerQuotes");

                while (resSettings.next()) {
                    String quote = resSettings.getString("quote");
                    String user = resSettings.getString("name");
                    EmbedUtils.footerQuotes.put(quote, user);
                }

                logger.debug("Loaded " + EmbedUtils.footerQuotes.size() + " quotes.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    database.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }*/

    /**
     * This will get the settings from our database and store them in the {@link Variables#GUILD_SETTINGS settings}
     */
    private static void loadGuildSettings() {
        logger.debug("Loading Guild settings.");

        String dbName = Variables.DATABASE.getName();
        Variables.DATABASE.run(() -> {
            Connection database = Variables.DATABASE.getConnManager().getConnection();
            try {
                Statement smt = database.createStatement();

                ResultSet res = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");

                while (res.next()) {
                    long guildId = toLong(res.getString("guildId"));

                    Variables.GUILD_SETTINGS.put(guildId, new GuildSettings(guildId)
                            .setEnableJoinMessage(res.getBoolean("enableJoinMessage"))
                            .setEnableSwearFilter(res.getBoolean("enableSwearFilter"))
                            .setCustomJoinMessage(replaceNewLines(res.getString("customWelcomeMessage")))
                            .setCustomPrefix(res.getString("prefix"))
                            .setLogChannel(toLong(res.getString("logChannelId")))
                            .setWelcomeLeaveChannel(toLong(res.getString("welcomeLeaveChannel")))
                            .setCustomLeaveMessage(replaceNewLines(res.getString("customLeaveMessage")))
                            .setAutoroleRole(toLong(res.getString("autoRole")))
                            .setServerDesc(replaceNewLines(res.getString("serverDesc")))
                            .setAnnounceTracks(res.getBoolean("announceNextTrack"))
                            .setAutoDeHoist(res.getBoolean("autoDeHoist"))
                            .setFilterInvites(res.getBoolean("filterInvites"))
                            .setEnableSpamFilter(res.getBoolean("spamFilterState"))
                            .setMuteRoleId(toLong(res.getString("muteRoleId")))
                            .setRatelimits(ratelimmitChecks(res.getString("ratelimits")))
                            .setKickState(res.getBoolean("kickInsteadState"))
                    );
                }

                logger.debug("Loaded settings for " + Variables.GUILD_SETTINGS.keySet().size() + " guilds.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    database.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild the guild to get
     * @return the guild
     */
    public static GuildSettings getGuild(Guild guild) {

        if (!Variables.GUILD_SETTINGS.containsKey(guild.getIdLong())) {
            return registerNewGuild(guild);
        }

        return Variables.GUILD_SETTINGS.get(guild.getIdLong());

    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     *
     * @param guild    The guild to update it for
     * @param settings the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings) {
        if (!Variables.GUILD_SETTINGS.containsKey(settings.getGuildId())) {
            registerNewGuild(guild);
            return;
        }
        Variables.DATABASE.run(() -> {
            String dbName = Variables.DATABASE.getName();
            Connection database = Variables.DATABASE.getConnManager().getConnection();

            try {
                PreparedStatement smt = database.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
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
                smt.setBoolean(13, settings.getEnableSpamFilter());
                smt.setString(14, String.valueOf(settings.getMuteRoleId()));
                smt.setString(15, convertJ2S(settings.getRatelimits()));
                smt.setBoolean(16, settings.getKickState());
                smt.executeUpdate();

            } catch (SQLException e1) {
                if (!e1.getLocalizedMessage().toLowerCase().startsWith("incorrect string value"))
                    e1.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    database.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    /**
     * This will register a new guild with their settings on bot join
     *
     * @param g The guild that we are joining
     * @return The new guild
     */
    public static GuildSettings registerNewGuild(Guild g) {
        if (Variables.GUILD_SETTINGS.containsKey(g.getIdLong())) {
            return Variables.GUILD_SETTINGS.get(g.getIdLong());
        }
        GuildSettings newGuildSettings = new GuildSettings(g.getIdLong());
        Variables.DATABASE.run(() -> {

            String dbName = Variables.DATABASE.getName();
            Connection database = Variables.DATABASE.getConnManager().getConnection();

            try {
                ResultSet resultSet = database.createStatement()
                        .executeQuery("SELECT id FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
                int rows = 0;
                while (resultSet.next())
                    rows++;

                if (rows == 0) {
                    PreparedStatement smt = database.prepareStatement("INSERT INTO " + dbName + ".guildSettings(guildId, guildName," +
                            "customWelcomeMessage, prefix, customLeaveMessage, ratelimits) " +
                            "VALUES('" + g.getId() + "',  ? , ? , ? , ? , ?)");
                    smt.setString(1, g.getName().replaceAll("\\P{Print}", ""));
                    smt.setString(2, newGuildSettings.getCustomJoinMessage());
                    smt.setString(3, Settings.PREFIX);
                    smt.setString(4, newGuildSettings.getCustomLeaveMessage().replaceAll("\\P{Print}", ""));
                    smt.setString(5, "20|45|60|120|240|2400".replaceAll("\\P{Print}", ""));
                    smt.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            Variables.GUILD_SETTINGS.put(g.getIdLong(), newGuildSettings);
        });
        return newGuildSettings;
    }

    /**
     * This will attempt to remove a guild wen we leave it
     *
     * @param g the guild to remove from the database
     */
    public static void deleteGuild(Guild g) {
        Variables.GUILD_SETTINGS.remove(g.getIdLong());
        Variables.DATABASE.run(() -> {
            String dbName = Variables.DATABASE.getName();
            Connection database = Variables.DATABASE.getConnManager().getConnection();

            try {
                Statement smt = database.createStatement();
                smt.execute("DELETE FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    database.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    private static String replaceNewLines(String entery) {
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
        }
        catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
