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
    public static void loadFooterQuotes() {
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
    public static void loadGuildSettings() {
        logger.debug("Loading Guild settings.");
        
        String dbName = AirUtils.DB.getName();
        
        Connection database = AirUtils.DB.getConnManager().getConnection();
        try {
            Statement smt = database.createStatement();
            
            ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");
            
            while (resSettings.next()) {
                String guildId = resSettings.getString("guildId");
                boolean enableJoinMsg = resSettings.getBoolean("enableJoinMessage");
                boolean enableSwearFilter = resSettings.getBoolean("enableSwearFilter");
                String joinmsg = replaceNewLines(resSettings.getString("customWelcomeMessage"));
                String prefix = resSettings.getString("prefix");
                String logChannel = resSettings.getString("logChannelId");
                String welcomeLeaveChannel = resSettings.getString("welcomeLeaveChannel");
                String leaveMessage = replaceNewLines(resSettings.getString("customLeaveMessage"));
                String autoroleId = resSettings.getString("autoRole");
                String serverDesc = replaceNewLines(resSettings.getString("serverDesc"));
                boolean announceNextTrack = resSettings.getBoolean("announceNextTrack");

                AirUtils.guildSettings.put(guildId, new GuildSettings(guildId)
                        .setEnableJoinMessage(enableJoinMsg)
                        .setEnableSwearFilter(enableSwearFilter)
                        .setCustomJoinMessage(joinmsg)
                        .setCustomPrefix(prefix)
                        .setLogChannel(logChannel)
                        .setWelcomeLeaveChannel(welcomeLeaveChannel)
                        .setCustomLeaveMessage(leaveMessage)
                        .setAutoroleRole(autoroleId)
                        .setServerDesc(serverDesc)
                        .setAnnounceTracks(announceNextTrack)
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
        
        String guildId = settings.getGuildId();
        boolean enableJoinMessage = settings.isEnableJoinMessage();
        boolean enableSwearFilter = settings.isEnableSwearFilter();
        String customJoinMessage = settings.getCustomJoinMessage();
        String customLeaveMessage = settings.getCustomLeaveMessage();
        String newPrefix = settings.getCustomPrefix();
        String autoRole = settings.getAutoroleRole();
        String chanId = settings.getLogChannel();
        String welcomeLeaveChannel = settings.getWelcomeLeaveChannel();
        String serverDesc = settings.getServerDesc();
        boolean announceNextTrack = settings.isAnnounceTracks();

        String dbName = AirUtils.DB.getName();
        Connection database = AirUtils.DB.getConnManager().getConnection();
        
        try {
            PreparedStatement preparedStatement = database.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
                    "enableJoinMessage= ? , " +
                    "enableSwearFilter= ? ," +
                    "customWelcomeMessage= ? ," +
                    "prefix= ? ," +
                    "autoRole= ? ," +
                    "logChannelId= ? ," +
                    "welcomeLeaveChannel= ? ," +
                    "customLeaveMessage = ? ," +
                    "serverDesc = ? ," +
                    "announceNextTrack = ? " +
                    "WHERE guildId='" + guildId + "'");
            preparedStatement.setBoolean(1, enableJoinMessage);
            preparedStatement.setBoolean(2, enableSwearFilter);
            preparedStatement.setString(3, replaceUnicode(customJoinMessage));
            preparedStatement.setNString(4, replaceUnicode(newPrefix));
            preparedStatement.setString(5, autoRole);
            preparedStatement.setString(6, chanId);
            preparedStatement.setString(7, welcomeLeaveChannel);
            preparedStatement.setString(8, replaceUnicode(customLeaveMessage));
            preparedStatement.setString(9, replaceUnicode(serverDesc));
            preparedStatement.setBoolean(10, announceNextTrack);
            preparedStatement.executeUpdate();

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
        
        final boolean ENABLE_JOIN_MSG = false;
        final boolean ENABLE_SWEAR_FILTER = false;
        String defaultMsg = "Welcome {{USER_MENTION}}, to the official {{GUILD_NAME}} guild.";
        GuildSettings newGuildSettings = new GuildSettings(g.getId())
                                             .setEnableJoinMessage(ENABLE_JOIN_MSG)
                                             .setEnableSwearFilter(ENABLE_SWEAR_FILTER)
                                             .setCustomJoinMessage(defaultMsg)
                                             .setCustomPrefix(Settings.PREFIX);
        
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
                        "VALUES('" + g.getId() + "',  ? ,'" + defaultMsg + "', ? , ?)");
                smt.setString(1, g.getName().replaceAll("\\P{Print}", ""));
                smt.setString(2, Settings.PREFIX);
                smt.setString(3, newGuildSettings.getCustomLeaveMessage().replaceAll("\\P{Print}", ""));
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
