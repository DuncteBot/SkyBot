/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
     * This will hold the custom prefix if the guild has set one
     */
    private String customPrefix = Settings.prefix;

    /**
     * This will init everything
     * @param guildId the id of the guild that the settings are for
     */
    public GuildSettings(String guildId) {
        this.guildId = guildId;
    }

    /**
     * We use this to update if the join message should display
     * @param enableJoinMessage whether we should display the join message
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setEnableJoinMessage(boolean enableJoinMessage){
        this.enableJoinMessage = enableJoinMessage;
        return this;
    }

    /**
     * We use this to update if we should block swearwords
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
     * This will set the custom prefix for the corresponding guild
     * @param customPrefix The new prefix
     * @return The current {@link GuildSettings}
     */
    public GuildSettings setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
        return this;
    }

    /**
     * this will check if the join message is enabled
     * @return true if the join message is enabled
     */
    public boolean isEnableJoinMessage() {
        return enableJoinMessage;
    }

    /**
     * This will check if the swear filter is enabled
     * @return true if the filter is on for this guild
     */
    public boolean isEnableSwearFilter() {
        return enableSwearFilter;
    }

    /**
     * This will return the guild id that these options are for
     * @return The id of that guild as a String
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * This will return the cutstom join message set for that guild
     * @return The custom join message
     */
    public String getCustomJoinMessage() {
        return customJoinMessage;
    }

    /**
     * Ths will return the prefix that the guild is using
     * @return The prefix that the guild is using
     */
    public String getCustomPrefix() {
        return customPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GuildSettings["+guildId+"](prefix="+customPrefix+", Swearword filter="+enableSwearFilter+", Join message="+enableJoinMessage+")";
    }
}
