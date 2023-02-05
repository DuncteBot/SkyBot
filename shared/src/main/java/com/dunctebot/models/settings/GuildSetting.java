/*
 * MIT License
 *
 * Copyright (c) 2020 Duncan Sterken
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
 *
 */

package com.dunctebot.models.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.dunctebot.models.utils.Utils.convertJ2S;
import static com.dunctebot.models.utils.Utils.ratelimmitChecks;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GuildSetting {
    public static String DEFAULT_PREFIX = "db!";
    public static final String[] LOGGING_TYPES = {"Invite", "Message", "Member", "Ban", "Unban", "Mute", "Kick", "Warn"};

    private final long guildId;
    private List<String> blacklistedWords = new ArrayList<>();
    private String prefix = DEFAULT_PREFIX;
    private long autoRole = 0L;
    private boolean enableJoinMessage = false;
    private boolean enableLeaveMessage = false;
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
    private ProfanityFilterType filterType = ProfanityFilterType.SEVERE_TOXICITY;
    private float aiSensitivity = 0.7f;
    private boolean allowAllToStop = false;
    // logging
    private boolean banLogging = true;
    private boolean unbanLogging = true;
    private boolean muteLogging = true;
    private boolean kickLogging = true;
    private boolean warnLogging = true;
    // new feature: disable by default
    private boolean memberLogging = false;
    private boolean inviteLoggingEnabled = false;
    private boolean messageLoggingEnabled = false;
    //
    private List<WarnAction> warnActions = List.of(
        new WarnAction(WarnAction.Type.KICK, 3)/*,
        new WarnAction(WarnAction.Type.TEMP_MUTE, 30, 4),
        new WarnAction(WarnAction.Type.TEMP_BAN, 5, 10),
        new WarnAction(WarnAction.Type.BAN, 10)*/
    );

    private int embedColor = 0x0751c6;

    private int youngAccountThreshold = -1;
    private boolean youngAccountBanEnabled = false;

    @JsonCreator
    public GuildSetting(@JsonProperty("guildId") long guildId) {
        this.guildId = guildId;
    }

    @JsonProperty("guildId")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getGuildId() {
        return this.guildId;
    }

    @JsonProperty("enableJoinMessage")
    public boolean isEnableJoinMessage() {
        return this.enableJoinMessage;
    }

    @JsonProperty("enableJoinMessage")
    public GuildSetting setEnableJoinMessage(boolean enableJoinMessage) {
        this.enableJoinMessage = enableJoinMessage;
        return this;
    }

    @JsonProperty("enableLeaveMessage")
    public boolean isEnableLeaveMessage() {
        return this.enableLeaveMessage;
    }

    @JsonProperty("enableLeaveMessage")
    public GuildSetting setEnableLeaveMessage(boolean enableLeaveMessage) {
        this.enableLeaveMessage = enableLeaveMessage;
        return this;
    }

    @JsonProperty("enableSwearFilter")
    public boolean isEnableSwearFilter() {
        return this.enableSwearFilter;
    }

    @JsonProperty("enableSwearFilter")
    public GuildSetting setEnableSwearFilter(boolean enableSwearFilter) {
        this.enableSwearFilter = enableSwearFilter;
        return this;
    }

    @JsonProperty("customWelcomeMessage")
    public String getCustomJoinMessage() {
        return this.customWelcomeMessage;
    }

    @JsonProperty("customWelcomeMessage")
    public GuildSetting setCustomJoinMessage(String customJoinMessage) {
        this.customWelcomeMessage = customJoinMessage;
        return this;
    }

    @JsonProperty("customLeaveMessage")
    public String getCustomLeaveMessage() {
        return this.customLeaveMessage;
    }

    @JsonProperty("customLeaveMessage")
    public GuildSetting setCustomLeaveMessage(String customLeaveMessage) {
        this.customLeaveMessage = customLeaveMessage;
        return this;
    }

    @JsonProperty("prefix")
    public String getCustomPrefix() {
        return this.prefix;
    }

    @JsonProperty("prefix")
    public GuildSetting setCustomPrefix(String customPrefix) {
        this.prefix = customPrefix;
        return this;
    }

    @JsonProperty("logChannelId")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getLogChannel() {
        return this.logChannelId;
    }

    @JsonProperty("logChannelId")
    public GuildSetting setLogChannel(long tc) {
        this.logChannelId = tc;

        return this;
    }

    @JsonProperty("autorole")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getAutoroleRole() {
        return this.autoRole;
    }

    @JsonProperty("autorole")
    public GuildSetting setAutoroleRole(long autoroleRole) {
        this.autoRole = autoroleRole;

        return this;
    }

    @JsonIgnore
    public boolean isAutoroleEnabled() {
        return this.autoRole > 0;
    }

    @JsonProperty("welcomeLeaveChannel")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getWelcomeLeaveChannel() {
        return this.welcomeLeaveChannel;
    }

    @JsonProperty("welcomeLeaveChannel")
    public GuildSetting setWelcomeLeaveChannel(long welcomeLeaveChannel) {
        this.welcomeLeaveChannel = welcomeLeaveChannel;

        return this;
    }

    @JsonProperty("serverDesc")
    public String getServerDesc() {
        return this.serverDesc;
    }

    @JsonProperty("serverDesc")
    public GuildSetting setServerDesc(String serverDesc) {
        this.serverDesc = serverDesc;

        return this;
    }

    @JsonProperty("announceNextTrack")
    public boolean isAnnounceTracks() {
        return this.announceNextTrack;
    }

    @JsonProperty("announceNextTrack")
    public GuildSetting setAnnounceTracks(boolean announceTracks) {
        this.announceNextTrack = announceTracks;

        return this;
    }

    @JsonProperty("autoDehoist")
    public boolean isAutoDeHoist() {
        return this.autoDeHoist;
    }

    @JsonProperty("autoDehoist")
    public GuildSetting setAutoDeHoist(boolean autoDeHoist) {
        this.autoDeHoist = autoDeHoist;

        return this;
    }

    @JsonProperty("filterInvites")
    public boolean isFilterInvites() {
        return this.filterInvites;
    }

    @JsonProperty("filterInvites")
    public GuildSetting setFilterInvites(boolean filterInvites) {
        this.filterInvites = filterInvites;

        return this;
    }

    @JsonProperty("spamFilterState")
    public boolean isEnableSpamFilter() {
        return this.spamFilterState;
    }

    @JsonProperty("spamFilterState")
    public GuildSetting setEnableSpamFilter(boolean newState) {
        spamFilterState = newState;

        return this;
    }

    @JsonProperty("muteRoleId")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getMuteRoleId() {
        return this.muteRoleId;
    }

    @JsonProperty("muteRoleId")
    public GuildSetting setMuteRoleId(long muteRoleId) {
        this.muteRoleId = muteRoleId;

        return this;
    }

    @JsonIgnore
    public long[] getRatelimits() {
        return this.ratelimits;
    }

    @JsonIgnore
    public GuildSetting setRatelimits(long[] ratelimits) {
        this.ratelimits = ratelimits;

        return this;
    }

    @JsonProperty("kickInsteadState")
    public boolean getKickState() {
        return this.kickInsteadState;
    }

    @JsonProperty("kickInsteadState")
    public GuildSetting setKickState(boolean newState) {
        kickInsteadState = newState;

        return this;
    }

    @JsonIgnore
    public List<String> getBlacklistedWords() {
        return this.blacklistedWords;
    }

    @JsonIgnore
    public GuildSetting setBlacklistedWords(List<String> blacklistedWords) {
        this.blacklistedWords = blacklistedWords;

        return this;
    }

    @JsonProperty("leave_timeout")
    public int getLeaveTimeout() {
        return this.leave_timeout;
    }

    @JsonProperty("leave_timeout")
    public GuildSetting setLeaveTimeout(int leaveTimeout) {
        this.leave_timeout = leaveTimeout;

        return this;
    }

    @JsonProperty("spam_threshold")
    public int getSpamThreshold() {
        return this.spam_threshold;
    }

    @JsonProperty("spam_threshold")
    public GuildSetting setSpamThreshold(int spamThreshold) {
        this.spam_threshold = spamThreshold;

        return this;
    }

    /// <editor-fold desc="logging settings" defaultstate="collapsed">
    @JsonProperty("banLogging")
    public boolean isBanLogging() {
        return this.banLogging;
    }

    @JsonProperty("banLogging")
    public GuildSetting setBanLogging(boolean banLogging) {
        this.banLogging = banLogging;
        return this;
    }

    @JsonProperty("unbanLogging")
    public boolean isUnbanLogging() {
        return this.unbanLogging;
    }

    @JsonProperty("unbanLogging")
    public GuildSetting setUnbanLogging(boolean unbanLogging) {
        this.unbanLogging = unbanLogging;
        return this;
    }

    @JsonProperty("muteLogging")
    public boolean isMuteLogging() {
        return this.muteLogging;
    }

    @JsonProperty("muteLogging")
    public GuildSetting setMuteLogging(boolean muteLogging) {
        this.muteLogging = muteLogging;
        return this;
    }

    @JsonProperty("kickLogging")
    public boolean isKickLogging() {
        return this.kickLogging;
    }

    @JsonProperty("kickLogging")
    public GuildSetting setKickLogging(boolean kickLogging) {
        this.kickLogging = kickLogging;
        return this;
    }

    @JsonProperty("warnLogging")
    public boolean isWarnLogging() {
        return this.warnLogging;
    }

    @JsonProperty("warnLogging")
    public GuildSetting setWarnLogging(boolean warnLogging) {
        this.warnLogging = warnLogging;
        return this;
    }

    @JsonProperty("memberLogging")
    public boolean isMemberLogging() {
        return this.memberLogging;
    }

    @JsonProperty("memberLogging")
    public GuildSetting setMemberLogging(boolean memberLogging) {
        this.memberLogging = memberLogging;
        return this;
    }

    @JsonProperty("invite_logging")
    public boolean isInviteLogging() {
        return this.inviteLoggingEnabled;
    }

    @JsonProperty("invite_logging")
    public GuildSetting setInviteLogging(boolean enabled) {
        this.inviteLoggingEnabled = enabled;

        return this;
    }

    @JsonProperty("message_logging")
    public boolean isMessageLogging() {
        return this.messageLoggingEnabled;
    }

    @JsonProperty("message_logging")
    public GuildSetting setMessageLogging(boolean enabled) {
        this.messageLoggingEnabled = enabled;
        return this;
    }
    /// </editor-fold>

    @JsonProperty("filterType")
    public ProfanityFilterType getFilterType() {
        return this.filterType;
    }

    @JsonIgnore
    public GuildSetting setFilterType(ProfanityFilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    @JsonProperty("aiSensitivity")
    public float getAiSensitivity() {
        return this.aiSensitivity;
    }

    @JsonProperty("aiSensitivity")
    public GuildSetting setAiSensitivity(float aiSensitivity) {
        this.aiSensitivity = aiSensitivity;
        return this;
    }

    @JsonProperty("allowAllToStop")
    public boolean isAllowAllToStop() {
        return this.allowAllToStop;
    }

    @JsonProperty("allowAllToStop")
    public GuildSetting setAllowAllToStop(boolean allowAllToStop) {
        this.allowAllToStop = allowAllToStop;
        return this;
    }

    @JsonProperty("warn_actions")
    public List<WarnAction> getWarnActions() {
        return this.warnActions;
    }

    @JsonProperty("warn_actions")
    public GuildSetting setWarnActions(List<WarnAction> warnings) {

        // if there are warn actions we can set them
        if (warnings.isEmpty()) {
            this.warnActions = List.of(new WarnAction(WarnAction.Type.KICK, 3));
        } else {
            this.warnActions = warnings;
        }

        return this;
    }

    @JsonIgnore
    public int getEmbedColor() {
        return embedColor;
    }

    @JsonIgnore
    public GuildSetting setEmbedColor(int embedColor) {
        this.embedColor = embedColor;
        return this;
    }

    @JsonProperty("young_account_threshold")
    public int getYoungAccountThreshold() {
        return this.youngAccountThreshold;
    }

    @JsonProperty("young_account_threshold")
    public GuildSetting setYoungAccountThreshold(int days) {
        this.youngAccountThreshold = days;
        return this;
    }

    @JsonProperty("young_account_ban_enabled")
    public boolean isYoungAccountBanEnabled() {
        return this.youngAccountBanEnabled;
    }

    @JsonProperty("young_account_ban_enabled")
    public GuildSetting setYoungAccountBanEnabled(boolean enabled) {
        this.youngAccountBanEnabled = enabled;
        return this;
    }

    @JsonIgnore
    public Object call(String methodName) {
        try {
            return this.getClass().getDeclaredMethod(methodName).invoke(this);
        }
        catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("GuildSetting[%s]", guildId);
    }

    // A utility method that might come in handy in the future (22-08-2018) https://github.com/DuncteBot/SkyBot/commit/4356e0ebc35798f963bff9b2b94396329f39463e#diff-d6b916869893fbd27dd3e469ac1ddc5a
    // The future is now (30-11-2018) https://github.com/DuncteBot/SkyBot/commit/eb0303d5d819060efd2c908dde9d477b8fcf189f#diff-d6b916869893fbd27dd3e469ac1ddc5a
    @JsonIgnore
    public ObjectNode toJson(ObjectMapper mapper) {
        return mapper.valueToTree(this);
    }

    ///////////////////////////////////////////////////
    // Jackson getters/setters for easy json conversion

    @JsonProperty("ratelimits")
    public GuildSetting setRatelimits(JsonNode ratelimits) {
        this.ratelimits = ratelimmitChecks(ratelimits.asText());

        return this;
    }

    @JsonProperty("ratelimits")
    public String getRatelimitsDb() {
        return convertJ2S(this.ratelimits);
    }

    @JsonProperty("filterType")
    public GuildSetting setFilterType(String filterType) {
        this.filterType = ProfanityFilterType.fromType(filterType);
        return this;
    }

    @JsonProperty("blacklisted_words")
    public GuildSetting setBlackListedWords(JsonNode blacklistedWords) {
        if (!blacklistedWords.isArray()) {
            throw new IllegalArgumentException("Not an array");
        }

        blacklistedWords.forEach(
            (json) -> this.blacklistedWords.add(json.get("word").asText())
        );

        return this;
    }

    @JsonProperty("embed_setting")
    public GuildSetting setEmbedSetting(JsonNode embedColor) {
        if (!embedColor.isNull()) {
            this.embedColor = embedColor.get("embed_color").asInt();
        }

        return this;
    }

    @JsonProperty("embed_setting")
    public JsonNode getEmbedSetting() {
        return new ObjectMapper()
            .createObjectNode()
            .put("embed_color", this.embedColor);
    }
}
