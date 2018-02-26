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

@SuppressWarnings("SqlDialectInspection")
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);

    /**
     * This runs both {@link #loadGuildSettings()} and {@link #loadFooterQuotes()}
     */
    public static void loadAllSettings() {
        loadGuildSettings();
        loadFooterQuotes();
    }

    /**
     * This will load all the footer quotes from the database and store them in the {@link  EmbedUtils#footerQuotes}
     */
    private static void loadFooterQuotes() {
        if(!AirUtils.NONE_SQLITE) return;
        logger.debug("Loading footer quotes");

        String dbName = AirUtils.DB.getName();
        
        Connection database = AirUtils.DB.getConnManager().getConnection();
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
    }

    /**
     * This will get the settings from our database and store them in the {@link AirUtils#guildSettings settings}
     */
    private static void loadGuildSettings() {
        logger.debug("Loading Guild settings.");
        
        String dbName = AirUtils.DB.getName();
        
        Connection database = AirUtils.DB.getConnManager().getConnection();
        try {
            Statement smt = database.createStatement();
            
            ResultSet res = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");
            
            while (res.next()) {
                String guildId = res.getString("guildId");

                AirUtils.guildSettings.put(guildId, new GuildSettings(guildId)
                        .setEnableJoinMessage(res.getBoolean("enableJoinMessage"))
                        .setEnableSwearFilter(res.getBoolean("enableSwearFilter"))
                        .setCustomJoinMessage(replaceNewLines(res.getString("customWelcomeMessage")))
                        .setCustomPrefix(res.getString("prefix"))
                        .setLogChannel(res.getString("logChannelId"))
                        .setWelcomeLeaveChannel(res.getString("welcomeLeaveChannel"))
                        .setCustomLeaveMessage(replaceNewLines(res.getString("customLeaveMessage")))
                        .setAutoroleRole(res.getString("autoRole"))
                        .setServerDesc(replaceNewLines(res.getString("serverDesc")))
                        .setAnnounceTracks(res.getBoolean("announceNextTrack"))
                        .setAutoDeHoist(res.getBoolean("autoDeHoist"))
                        .setFilterInvites(res.getBoolean("filterInvites"))
                );
            }

            logger.debug("Loaded settings for " + AirUtils.guildSettings.keySet().size() + " guilds.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild the guild to get
     * @return the guild
     */
    public static GuildSettings getGuild(Guild guild) {
        
        if (!AirUtils.guildSettings.containsKey(guild.getId())) {
            return registerNewGuild(guild);
        }
        
        return AirUtils.guildSettings.get(guild.getId());
        
    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     *
     * @param guild    The guild to update it for
     * @param settings the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings) {
        if (!AirUtils.guildSettings.containsKey(settings.getGuildId())) {
            registerNewGuild(guild);
            return;
        }

        String dbName = AirUtils.DB.getName();
        Connection database = AirUtils.DB.getConnManager().getConnection();
        
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
                    "filterInvites = ? " +
                    "WHERE guildId='" + settings.getGuildId() + "'");
            smt.setBoolean(1, settings.isEnableJoinMessage());
            smt.setBoolean(2, settings.isEnableSwearFilter());
            smt.setString(3, replaceUnicode(settings.getCustomJoinMessage()));
            smt.setString(4, replaceUnicode(settings.getCustomPrefix()));
            smt.setString(5, settings.getAutoroleRole());
            smt.setString(6, settings.getLogChannel());
            smt.setString(7, settings.getWelcomeLeaveChannel());
            smt.setString(8, replaceUnicode(settings.getCustomLeaveMessage()));
            smt.setString(9, replaceUnicode(settings.getServerDesc()));
            smt.setBoolean(10, settings.isAnnounceTracks());
            smt.setBoolean(11, settings.isAutoDeHoist());
            smt.setBoolean(12, settings.isFilterInvites());
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
    }

    /**
     * This will register a new guild with their settings on bot join
     *
     * @param g The guild that we are joining
     * @return The new guild
     */
    public static GuildSettings registerNewGuild(Guild g) {
        if (AirUtils.guildSettings.containsKey(g.getId())) {
            return AirUtils.guildSettings.get(g.getId());
        }
        GuildSettings newGuildSettings = new GuildSettings(g.getId());
        
        String dbName = AirUtils.DB.getName();
        Connection database = AirUtils.DB.getConnManager().getConnection();
        
        try {
            ResultSet resultSet = database.createStatement()
                                  .executeQuery("SELECT id FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
            int rows = 0;
            while (resultSet.next())
                rows++;
            
            if (rows == 0) {
                PreparedStatement smt = database.prepareStatement("INSERT INTO " + dbName + ".guildSettings(guildId, guildName," +
                                                                          "customWelcomeMessage, prefix, customLeaveMessage) " +
                        "VALUES('" + g.getId() + "',  ? , ? , ? , ?)");
                smt.setString(1, g.getName().replaceAll("\\P{Print}", ""));
                smt.setString(2, newGuildSettings.getCustomJoinMessage());
                smt.setString(3, Settings.PREFIX);
                smt.setString(4, newGuildSettings.getCustomLeaveMessage().replaceAll("\\P{Print}", ""));
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
        AirUtils.guildSettings.put(g.getId(), newGuildSettings);
        return newGuildSettings;
    }

    /**
     * This will attempt to remove a guild wen we leave it
     *
     * @param g the guild to remove from the database
     */
    public static void deleteGuild(Guild g) {
        AirUtils.guildSettings.remove(g.getId());
        
        String dbName = AirUtils.DB.getName();
        Connection database = AirUtils.DB.getConnManager().getConnection();
        
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
    }

    private static String replaceNewLines(String entery) {
        if(entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\\\n", "\n");
    }

    private static String replaceUnicode(String entery) {
        if(entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\P{Print}", "");
    }
}
