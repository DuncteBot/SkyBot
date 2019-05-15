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

package ml.duncte123.skybot.objects.guild;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Settings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ml.duncte123.skybot.utils.GuildSettingsUtils.convertJ2S;
import static ml.duncte123.skybot.utils.GuildSettingsUtils.ratelimmitChecks;

/**
 * This class will hold the settings for a guild
 */
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildSettings {

    private final long guildId;
    private final List<String> blacklistedWords = new ArrayList<>();
    private String prefix = Settings.PREFIX;
    private long autoRole = 0L;
    private boolean enableJoinMessage = false;
    private boolean enableSwearFilter = false;
    private boolean autoDeHoist = false;
    private boolean filterInvites = false;
    private boolean announceNextTrack = false;
    private String customWelcomeMessage = "Welcome {atuser}, to the official **{server}** guild.";
    private String customLeaveMessage = "**{user}** has left **{server}** :worried:";
    private String serverDesc = "";
    private long logChannelId = 0L;
    private long welcomeLeaveChannel = 0L;
    private boolean spamFilterState = false;
    private boolean kickInsteadState = false;
    private long muteRoleId = 0L;
    private long[] ratelimits = {20L, 45L, 60L, 120L, 240L, 2400L};
    private int leave_timeout = 1;
    private int spam_threshold = 7;

    /**
     * This will init everything
     *
     * @param guildId
     *         the id of the guild that the settings are for
     */
    @JsonCreator
    public GuildSettings(@JsonProperty("guildId") long guildId) {
        this.guildId = guildId;
    }

    /**
     * this will check if the join message is enabled
     *
     * @return true if the join message is enabled
     */
    @JsonProperty("enableJoinMessage")
    public boolean isEnableJoinMessage() {
        return enableJoinMessage;
    }

    /**
     * We use this to update if the join message should display
     *
     * @param enableJoinMessage
     *         whether we should display the join message
     *
     * @return The current {@link GuildSettings}
     */
    @JsonProperty("enableJoinMessage")
    public GuildSettings setEnableJoinMessage(boolean enableJoinMessage) {
        this.enableJoinMessage = enableJoinMessage;
        return this;
    }

    /**
     * This will check if the swear filter is enabled
     *
     * @return true if the filter is on for this guild
     */
    @JsonProperty("enableSwearFilter")
    public boolean isEnableSwearFilter() {
        return enableSwearFilter;
    }

    /**
     * We use this to update if we should block swearwords
     *
     * @param enableSwearFilter
     *         whether we should block swearing
     *
     * @return The current {@link GuildSettings}
     */
    @JsonProperty("enableSwearFilter")
    public GuildSettings setEnableSwearFilter(boolean enableSwearFilter) {
        this.enableSwearFilter = enableSwearFilter;
        return this;
    }

    /**
     * This will return the guild id that these options are for
     *
     * @return The id of that guild as a String
     */
    @JsonIgnore
    public long getGuildId() {
        return guildId;
    }

    /**
     * This will return the custom join message set for that guild
     *
     * @return The custom join message
     */
    @JsonProperty("customWelcomeMessage")
    public String getCustomJoinMessage() {
        return customWelcomeMessage;
    }

    /**
     * This will set the custom join for the corresponding guild
     *
     * @param customJoinMessage
     *         The new join message
     *
     * @return The current {@link GuildSettings}
     */
    @JsonProperty("customWelcomeMessage")
    public GuildSettings setCustomJoinMessage(String customJoinMessage) {
        this.customWelcomeMessage = customJoinMessage;
        return this;
    }

    /**
     * Returns the custom leave message
     *
     * @return the custom leave message
     */
    @JsonProperty("customLeaveMessage")
    public String getCustomLeaveMessage() {
        return customLeaveMessage;
    }

    /**
     * This will set the custom leave message for the corresponding guild
     *
     * @param customLeaveMessage
     *         The new leave message
     *
     * @return The current {@link GuildSettings}
     */
    @JsonProperty("customLeaveMessage")
    public GuildSettings setCustomLeaveMessage(String customLeaveMessage) {
        this.customLeaveMessage = customLeaveMessage;
        return this;
    }

    /**
     * Ths will return the prefix that the guild is using
     *
     * @return The prefix that the guild is using
     */
    @JsonProperty("prefix")
    public String getCustomPrefix() {
        return prefix;
    }

    /**
     * This will set the custom prefix for the corresponding guild
     *
     * @param customPrefix
     *         The new prefix
     *
     * @return The current {@link GuildSettings}
     */
    @JsonProperty("prefix")
    public GuildSettings setCustomPrefix(String customPrefix) {
        this.prefix = customPrefix;
        return this;
    }

    /**
     * Returns the channel to log in
     *
     * @return the channel to log in
     */
    @JsonProperty("logChannelId")
    public long getLogChannel() {
        return logChannelId;
    }

    /**
     * This will set the channel that we log all the mod stuff in
     *
     * @param tc
     *         the channel to log
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("logChannelId")
    public GuildSettings setLogChannel(long tc) {
        this.logChannelId = tc;

        return this;
    }

    /**
     * Returns the role id for the autorole feature
     *
     * @return the role id for the autorole feature
     */
    @JsonProperty("autorole")
    public long getAutoroleRole() {
        return autoRole;
    }

    /**
     * This sets the role id for the autorole
     *
     * @param autoroleRole
     *         the role to set the autorole to
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("autorole")
    public GuildSettings setAutoroleRole(long autoroleRole) {
        this.autoRole = autoroleRole;

        return this;
    }

    @JsonIgnore
    public boolean isAutoroleEnabled() {
        return this.autoRole > 0;
    }

    /**
     * Returns the channel in where the welcome or leave messages should display
     *
     * @return the channel in where the welcome or leave messages should display
     */
    @JsonProperty("welcomeLeaveChannel")
    public long getWelcomeLeaveChannel() {
        return welcomeLeaveChannel;
    }

    /**
     * This sets the channel in where the welcome or leave messages should display
     *
     * @param welcomeLeaveChannel
     *         the channel in where the welcome or leave messages should display
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("welcomeLeaveChannel")
    public GuildSettings setWelcomeLeaveChannel(long welcomeLeaveChannel) {
        this.welcomeLeaveChannel = welcomeLeaveChannel;

        return this;
    }

    /**
     * Returns the custom server description
     *
     * @return the custom server description
     */
    @JsonProperty("serverDesc")
    public String getServerDesc() {
        return serverDesc;
    }

    /**
     * Sets the current sever description to show up in DB!guildinfo
     *
     * @param serverDesc
     *         the custom server description
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("serverDesc")
    public GuildSettings setServerDesc(String serverDesc) {
        this.serverDesc = serverDesc;

        return this;
    }

    /**
     * Returns if we should announce the next track
     *
     * @return if we should announce the next track
     */
    @JsonProperty("announceNextTrack")
    public boolean isAnnounceTracks() {
        return announceNextTrack;
    }

    /**
     * Sets if the audio player should announce the tracks
     *
     * @param announceTracks
     *         true to announce tracks
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("announceNextTrack")
    public GuildSettings setAnnounceTracks(boolean announceTracks) {
        this.announceNextTrack = announceTracks;

        return this;
    }

    /**
     * Returns if we should auto de-hoist people (soon™)
     *
     * @return if we should auto de-hoist people (soon™)
     */
    @JsonProperty("autoDehoist")
    public boolean isAutoDeHoist() {
        return autoDeHoist;
    }

    /**
     * This sets if we should auto de-hoist people
     *
     * @param autoDeHoist
     *         if we should auto de-hoist people
     *
     * @return the current {@link GuildSettings}
     */
    @JsonProperty("autoDehoist")
    public GuildSettings setAutoDeHoist(boolean autoDeHoist) {
        this.autoDeHoist = autoDeHoist;

        return this;
    }

    /**
     * Returns if we should filter discord invites
     *
     * @return if we should filter discord invites
     */
    @JsonProperty("filterInvites")
    public boolean isFilterInvites() {
        return filterInvites;
    }

    /**
     * @param filterInvites
     *         Sets if we should filter out invites in messages
     *
     * @return the current settings for chaining
     */
    @JsonProperty("filterInvites")
    public GuildSettings setFilterInvites(boolean filterInvites) {
        this.filterInvites = filterInvites;

        return this;
    }

    @JsonProperty("spamFilterState")
    public boolean isEnableSpamFilter() {
        return spamFilterState;
    }

    @JsonProperty("spamFilterState")
    public GuildSettings setEnableSpamFilter(boolean newState) {
        spamFilterState = newState;

        return this;
    }

    @JsonProperty("muteRoleId")
    public long getMuteRoleId() {
        return muteRoleId;
    }

    @JsonProperty("muteRoleId")
    public GuildSettings setMuteRoleId(long muteRoleId) {
        this.muteRoleId = muteRoleId;

        return this;
    }

    @JsonProperty("ratelimits")
    public long[] getRatelimits() {
        return ratelimits;
    }

    @SuppressWarnings("unused") // Used to deserialize data
    @JsonProperty("ratelimits")
    public GuildSettings setRatelimits(JsonNode ratelimits) {
        this.ratelimits = ratelimmitChecks(ratelimits.asText());

        return this;
    }

    @JsonIgnore
    public GuildSettings setRatelimits(long[] ratelimits) {
        this.ratelimits = ratelimits;

        return this;
    }

    @SuppressWarnings("unused") // This is used in twig but not detected by your ide
    // because for some reason twig casts long[] to an object
    @JsonIgnore
    public Long[] getRateLimitsForTwig() {
        return Arrays.stream(ratelimits).boxed().toArray(Long[]::new);
    }

    @JsonProperty("kickInsteadState")
    public boolean getKickState() {
        return kickInsteadState;
    }

    @JsonProperty("kickInsteadState")
    public GuildSettings setKickState(boolean newState) {
        kickInsteadState = newState;

        return this;
    }

    @JsonIgnore
    public List<String> getBlacklistedWords() {
        return blacklistedWords;
    }

    @JsonIgnore
    public GuildSettings setBlacklistedWords(List<String> blacklistedWords) {
        this.blacklistedWords.clear();
        this.blacklistedWords.addAll(blacklistedWords);

        return this;
    }

    @SuppressWarnings("unused") // Used to deserialize data
    @JsonProperty("blacklisted_words")
    public GuildSettings setBlackListedWords(JsonNode blacklistedWords) {
        if (!blacklistedWords.isArray()) {
            throw new IllegalArgumentException("Not an array");
        }

        blacklistedWords.forEach(
            (json) -> this.blacklistedWords.add(json.get("word").asText())
        );

        return this;
    }

    @JsonProperty("leave_timeout")
    public int getLeaveTimeout() {
        return leave_timeout;
    }

    @JsonProperty("leave_timeout")
    public GuildSettings setLeaveTimeout(int leaveTimeout) {
        this.leave_timeout = leaveTimeout;

        return this;
    }

    @JsonProperty("spam_threshold")
    public int getSpamThreshold() {
        return spam_threshold;
    }

    @JsonProperty("spam_threshold")
    public GuildSettings setSpamThreshold(int spamThreshold) {
        this.spam_threshold = spamThreshold;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("GuildSettings[%s]", guildId);
    }

    // A utility method that might come in handy in the future (22-08-2018) https://github.com/DuncteBot/SkyBot/commit/4356e0ebc35798f963bff9b2b94396329f39463e#diff-d6b916869893fbd27dd3e469ac1ddc5a
    // The future is now (30-11-2018) https://github.com/DuncteBot/SkyBot/commit/eb0303d5d819060efd2c908dde9d477b8fcf189f#diff-d6b916869893fbd27dd3e469ac1ddc5a
    public ObjectNode toJson(ObjectMapper mapper) {
        final GuildSettings obj = this;
        final ObjectNode j = mapper.createObjectNode();

        for (final Field field : obj.getClass().getDeclaredFields()) {
            try {
                final String name = field.getName();

                // Blacklisted words are done separately
                if ("blacklistedWords".equals(name)) {
                    continue;
                }

                final Object value = field.get(obj);

                if ("ratelimits".equals(name)) {
                    j.put(name, convertJ2S((long[]) value));

                    continue;
                }

                if (value instanceof Boolean) {
                    j.put(name, (boolean) value);

                    continue;
                }

                j.put(name, String.valueOf(value));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return j;
    }
}
