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

package ml.duncte123.skybot.objects.guild;

import ml.duncte123.skybot.utils.Settings;

/**
 * This class will hold the settings for a guild
 */
public class GuildSettings {

    /**
     * the id of the guild that the settings are for
     */
    private String guildId;

    /**
     * if we should enable the join messages
     */
    private boolean enableJoinMessage = false;

    /**
     * if we should enable the swear filter
     */
    private boolean enableSwearFilter = false;

    /**
     * This will hold the custom join message
     */
    private String customJoinMessage = "Welcome {{USER_MENTION}}, to the official **{{GUILD_NAME}}** guild.";

    /**
     * This will hold the custom leave message
     */
    private String customLeaveMessage = "**{{USER_NAME}}** has left **{{GUILD_NAME}}** :worried:";

    /**
     * This will hold the custom prefix if the guild has set one
     */
    private String customPrefix = Settings.prefix;

    /**
     * This stores the channel that we log the bans in
     */
    private String logChannel = null;

    /**
     * This stores the channel in where the welcome or leave messages should display
     */
    private String welcomeLeaveChannel = null;

    private String autoroleRole = "";

    /**
     * This will init everything
     *
     * @param guildId the id of the guild that the settings are for
     */
    public GuildSettings(String guildId) {
        this.guildId = guildId;
    }

    /**
     * this will check if the join message is enabled
     *
     * @return true if the join message is enabled
     */
    public boolean isEnableJoinMessage() {
        return enableJoinMessage;
    }

    /**
     * We use this to update if the join message should display
     *
     * @param enableJoinMessage whether we should display the join message
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setEnableJoinMessage(boolean enableJoinMessage) {
        this.enableJoinMessage = enableJoinMessage;
        return this;
    }

    /**
     * This will check if the swear filter is enabled
     *
     * @return true if the filter is on for this guild
     */
    public boolean isEnableSwearFilter() {
        return enableSwearFilter;
    }

    /**
     * We use this to update if we should block swearwords
     *
     * @param enableSwearFilter whether we should block swearing
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setEnableSwearFilter(boolean enableSwearFilter) {
        this.enableSwearFilter = enableSwearFilter;
        return this;
    }

    /**
     * This will set the custom join for the corresponding guild
     * @param customJoinMessage The new join message
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setCustomJoinMessage(String customJoinMessage) {
        this.customJoinMessage = customJoinMessage;
        return this;
    }

    /**
     * This will set the custom leave message for the corresponding guild
     * @param customLeaveMessage The new leave message
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setCustomLeaveMessage(String customLeaveMessage) {
        this.customLeaveMessage = customLeaveMessage;
        return this;
    }

    /**
     * This will set the channel that we log all the mod stuff in
     * @param tc the channel to log
     * @return the current {@link GuildSettings}
     */
    public GuildSettings setLogChannel(String tc) {
        this.logChannel = tc;
        return this;
    }

    /**
     * This sets the channel in where the welcome or leave messages should display
     * @param welcomeLeaveChannel the channel in where the welcome or leave messages should display
     * @return the current {@link GuildSettings}
     */
    public GuildSettings setWelcomeLeaveChannel(String welcomeLeaveChannel) {
        this.welcomeLeaveChannel = welcomeLeaveChannel;
        return this;
    }

    /**
     * This will set the custom prefix for the corresponding guild
     *
     * @param customPrefix The new prefix
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
        return this;
    }

    /**
     * This sets the role id for the autorole
     *
     * @param autoroleRole the role to set the autorole to
     * @return the current {@link GuildSettings}
     */
    public GuildSettings setAutoroleRole(String autoroleRole) {
        this.autoroleRole = autoroleRole;
        return this;
    }

    /**
     * This will return the guild id that these options are for
     *
     * @return The id of that guild as a String
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * This will return the custom join message set for that guild
     *
     * @return The custom join message
     */
    public String getCustomJoinMessage() {
        return customJoinMessage;
    }

    /**
     * Returns the custom leave message
     * @return the custom leave message
     */
    public String getCustomLeaveMessage() {
        return customLeaveMessage;
    }

    /**
     * Ths will return the prefix that the guild is using
     *
     * @return The prefix that the guild is using
     */
    public String getCustomPrefix() {
        return customPrefix;
    }

    /**
     * Returns the channel to log in
     * @return the channel to log in
     */
    public String getLogChannel() {
        return logChannel;
    }

    /**
     * Returns the role id for the autorole feature
     * @return the role id for the autorole feature
     */
    public String getAutoroleRole() {
        return autoroleRole;
    }

    /**
     * Returns the channel in where the welcome or leave messages should display
     * @return the channel in where the welcome or leave messages should display
     */
    public String getWelcomeLeaveChannel() {
        return welcomeLeaveChannel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("GuildSettings[%s](prefix=%s, Swearword filter=%s, autorole id=%s)", guildId, customPrefix,
                (enableSwearFilter ? "Enabled" : "Disabled"), autoroleRole);
    }
}
