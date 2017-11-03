/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.slf4j.event.Level;

import java.sql.*;

public class GuildSettingsUtils {
    /**
     * This runs both {@link #loadGuildSettings()} and {{@link #loadFooterQuotes()}}
     */
    public static void loadAllSettings() {
        if(AirUtils.db.isConnected()) {
            loadGuildSettings();
            loadFooterQuotes();
        }
    }

    /**
     * This will load all the footer quotes from the database and store them in the {@link EmbedUtils#footerQuotes footerQuotes}
     */
    public static void loadFooterQuotes() {
        AirUtils.log(Level.INFO, "Clearing footer quotes");
        EmbedUtils.footerQuotes.clear();
        AirUtils.log(Level.INFO, "Loading footer quotes");
        
        //One default quote for now
        EmbedUtils.footerQuotes.put("I want your quotes", "duncte123");

        String dbName = AirUtils.db.getName();

        Connection database = AirUtils.db.getConnManager().getConnection();
        try {
            Statement smt = database.createStatement();

            ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".footerQuotes");

            while (resSettings.next()) {
                String quote = resSettings.getString("quote");
                String user = resSettings.getString("name");
                EmbedUtils.footerQuotes.put(quote, user);
            }

            AirUtils.log(Level.INFO, "Loaded "+ EmbedUtils.footerQuotes.size() +" quotes.");
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * This will get the settings from our database and store them in the {@link AirUtils#guildSettings settings}
     */
    public static void loadGuildSettings() {
        AirUtils.log(Level.INFO, "Loading Guild settings.");

        String dbName = AirUtils.db.getName();

        Connection database = AirUtils.db.getConnManager().getConnection();
        try {
            Statement smt = database.createStatement();

            ResultSet resSettings = smt.executeQuery("SELECT * FROM " + dbName + ".guildSettings");

            while (resSettings.next()) {
                String guildId = resSettings.getString("guildId");
                boolean enableJoinMsg = resSettings.getBoolean("enableJoinMessage");
                boolean enableSwearFilter = resSettings.getBoolean("enableSwearFilter");
                String joinmsg = resSettings.getString("customWelcomeMessage");
                String prefix = resSettings.getString("prefix");

                GuildSettings settings = new GuildSettings(guildId)
                        .setEnableJoinMessage(enableJoinMsg)
                        .setEnableSwearFilter(enableSwearFilter)
                        .setCustomJoinMessage(joinmsg)
                        .setCustomPrefix(prefix);

                AirUtils.guildSettings.put(guildId, settings);
            }

            AirUtils.log(Level.INFO, "Loaded settings for "+ AirUtils.guildSettings.keySet().size()+" guilds.");
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                database.close();
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * This wil get a guild or register it if it's not there yet
     * @param guild the guild to get
     * @return the guild
     */
    public static GuildSettings getGuild(Guild guild) {

        if(!AirUtils.guildSettings.containsKey(guild.getId())) {
            return registerNewGuild(guild);
        }

        return AirUtils.guildSettings.get(guild.getId());

    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     * @param guild The guild to update it for
     * @param settings the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings) {


        if(!AirUtils.guildSettings.containsKey(settings.getGuildId())) {
            registerNewGuild(guild);
            return;
        }

        String guildId = settings.getGuildId();
        boolean enableJoinMessage = settings.isEnableJoinMessage();
        boolean enableSwearFilter = settings.isEnableSwearFilter();
        String customJoinMessage = settings.getCustomJoinMessage();
        String newPrefix = settings.getCustomPrefix();

        if(AirUtils.db.isConnected()) {
            String dbName = AirUtils.db.getName();
            Connection database = AirUtils.db.getConnManager().getConnection();

            try {
                PreparedStatement preparedStatement = database.prepareStatement("UPDATE " + dbName + ".guildSettings SET " +
                        "enableJoinMessage= ? , " +
                        "enableSwearFilter= ? ," +
                        "customWelcomeMessage= ? ," +
                        "prefix= ? " +
                        "WHERE guildId='" + guildId + "'");
                preparedStatement.setBoolean(1, enableJoinMessage);
                preparedStatement.setBoolean(2, enableSwearFilter);
                preparedStatement.setString(3, customJoinMessage);
                preparedStatement.setString(4, newPrefix);
                preparedStatement.executeUpdate();

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
    }

    /**
     * This will register a new guild with their settings on bot join
     * @param g The guild that we are joining
     */
    public static GuildSettings registerNewGuild(Guild g) {

        if(AirUtils.guildSettings.containsKey(g.getId())) {
            return AirUtils.guildSettings.get(g.getId());
        }

        boolean ENABLE_JOIN_MSG = false;
        boolean ENABLE_SWEAR_FILTER = false;
        String defaultMsg = "Welcome {{USER_MENTION}}, to the official {{GUILD_NAME}} guild.";
        GuildSettings newGuildSettings = new GuildSettings(g.getId())
                .setEnableJoinMessage(ENABLE_JOIN_MSG)
                .setEnableSwearFilter(ENABLE_SWEAR_FILTER)
                .setCustomJoinMessage(defaultMsg)
                .setCustomPrefix(Settings.prefix);

        if(AirUtils.db.isConnected()) {
            String dbName = AirUtils.db.getName();

            Connection database = AirUtils.db.getConnManager().getConnection();

            try {

                ResultSet resultSet = database.createStatement().executeQuery("SELECT id FROM " + dbName + ".guildSettings WHERE guildId='" + g.getId() + "'");
                int rows = 0;
                while (resultSet.next()) {
                    rows++;
                }

                if (rows == 0) {
                    PreparedStatement smt = database.prepareStatement("INSERT INTO " + dbName + ".guildSettings(guildId, guildName, customWelcomeMessage) VALUES('" + g.getId() + "',  ? ,'" + defaultMsg + "')");
                    smt.setString(1, g.getName().replaceAll("\\P{Print}", ""));
                    smt.execute();
                }
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
        AirUtils.guildSettings.put(g.getId(), newGuildSettings);
        return newGuildSettings;
    }

    /**
     * This will attempt to remove a guild wen we leave it
     * @param g the guild to remove from the database
     */
    public static void deleteGuild(Guild g) {
        if(AirUtils.guildSettings.containsKey(g.getId())) {
            AirUtils.guildSettings.remove(g.getId());
        }
        if(AirUtils.db.isConnected()) {
            String dbName = AirUtils.db.getName();
            Connection database = AirUtils.db.getConnManager().getConnection();

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
    }

}
