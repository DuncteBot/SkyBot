/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Settings;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static ml.duncte123.skybot.utils.GuildSettingsUtils.convertJ2S;
import static ml.duncte123.skybot.utils.GuildSettingsUtils.ratelimmitChecks;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
@SuppressWarnings("unused")
public class GuildSettings {
    public static final String[] LOGGING_TYPES = {"Ban", "Unban", "Mute", "Kick", "Warn"};

    private final long guildId;
    private List<String> blacklistedWords = new ArrayList<>();
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
    private ProfanityFilterType filterType = ProfanityFilterType.SEVERE_TOXICITY;
    private float aiSensitivity = 0.7f;
    private boolean allowAllToStop = false;
    // logging
    private boolean banLogging = true;
    private boolean unbanLogging = true;
    private boolean muteLogging = true;
    private boolean kickLogging = true;
    private boolean warnLogging = true;
    //
    private List<WarnAction> warnActions = List.of(
        new WarnAction(WarnAction.Type.KICK, 3)/*,
        new WarnAction(WarnAction.Type.TEMP_MUTE, 30, 4),
        new WarnAction(WarnAction.Type.TEMP_BAN, 5, 10),
        new WarnAction(WarnAction.Type.BAN, 10)*/
    );

    @JsonCreator
    public GuildSettings(@JsonProperty("guildId") long guildId) {
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
    public GuildSettings setEnableJoinMessage(boolean enableJoinMessage) {
        this.enableJoinMessage = enableJoinMessage;
        return this;
    }

    @JsonProperty("enableSwearFilter")
    public boolean isEnableSwearFilter() {
        return this.enableSwearFilter;
    }

    @JsonProperty("enableSwearFilter")
    public GuildSettings setEnableSwearFilter(boolean enableSwearFilter) {
        this.enableSwearFilter = enableSwearFilter;
        return this;
    }

    @JsonProperty("customWelcomeMessage")
    public String getCustomJoinMessage() {
        return this.customWelcomeMessage;
    }

    @JsonProperty("customWelcomeMessage")
    public GuildSettings setCustomJoinMessage(String customJoinMessage) {
        this.customWelcomeMessage = customJoinMessage;
        return this;
    }

    @JsonProperty("customLeaveMessage")
    public String getCustomLeaveMessage() {
        return this.customLeaveMessage;
    }

    @JsonProperty("customLeaveMessage")
    public GuildSettings setCustomLeaveMessage(String customLeaveMessage) {
        this.customLeaveMessage = customLeaveMessage;
        return this;
    }

    @JsonProperty("prefix")
    public String getCustomPrefix() {
        return this.prefix;
    }

    @JsonProperty("prefix")
    public GuildSettings setCustomPrefix(String customPrefix) {
        this.prefix = customPrefix;
        return this;
    }

    @JsonProperty("logChannelId")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getLogChannel() {
        return this.logChannelId;
    }

    @JsonProperty("logChannelId")
    public GuildSettings setLogChannel(long tc) {
        this.logChannelId = tc;

        return this;
    }

    @JsonProperty("autorole")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getAutoroleRole() {
        return this.autoRole;
    }

    @JsonProperty("autorole")
    public GuildSettings setAutoroleRole(long autoroleRole) {
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
    public GuildSettings setWelcomeLeaveChannel(long welcomeLeaveChannel) {
        this.welcomeLeaveChannel = welcomeLeaveChannel;

        return this;
    }

    @JsonProperty("serverDesc")
    public String getServerDesc() {
        return this.serverDesc;
    }

    @JsonProperty("serverDesc")
    public GuildSettings setServerDesc(String serverDesc) {
        this.serverDesc = serverDesc;

        return this;
    }

    @JsonProperty("announceNextTrack")
    public boolean isAnnounceTracks() {
        return this.announceNextTrack;
    }

    @JsonProperty("announceNextTrack")
    public GuildSettings setAnnounceTracks(boolean announceTracks) {
        this.announceNextTrack = announceTracks;

        return this;
    }

    @JsonProperty("autoDehoist")
    public boolean isAutoDeHoist() {
        return this.autoDeHoist;
    }

    @JsonProperty("autoDehoist")
    public GuildSettings setAutoDeHoist(boolean autoDeHoist) {
        this.autoDeHoist = autoDeHoist;

        return this;
    }

    @JsonProperty("filterInvites")
    public boolean isFilterInvites() {
        return this.filterInvites;
    }

    @JsonProperty("filterInvites")
    public GuildSettings setFilterInvites(boolean filterInvites) {
        this.filterInvites = filterInvites;

        return this;
    }

    @JsonProperty("spamFilterState")
    public boolean isEnableSpamFilter() {
        return this.spamFilterState;
    }

    @JsonProperty("spamFilterState")
    public GuildSettings setEnableSpamFilter(boolean newState) {
        spamFilterState = newState;

        return this;
    }

    @JsonProperty("muteRoleId")
    @JsonSerialize(using = ToStringSerializer.class)
    public long getMuteRoleId() {
        return this.muteRoleId;
    }

    @JsonProperty("muteRoleId")
    public GuildSettings setMuteRoleId(long muteRoleId) {
        this.muteRoleId = muteRoleId;

        return this;
    }

    @JsonIgnore
    public long[] getRatelimits() {
        return this.ratelimits;
    }

    @JsonIgnore
    public GuildSettings setRatelimits(long[] ratelimits) {
        this.ratelimits = ratelimits;

        return this;
    }

    @JsonProperty("kickInsteadState")
    public boolean getKickState() {
        return this.kickInsteadState;
    }

    @JsonProperty("kickInsteadState")
    public GuildSettings setKickState(boolean newState) {
        kickInsteadState = newState;

        return this;
    }

    @JsonIgnore
    public List<String> getBlacklistedWords() {
        return this.blacklistedWords;
    }

    @JsonIgnore
    public GuildSettings setBlacklistedWords(List<String> blacklistedWords) {
        this.blacklistedWords = blacklistedWords;

        return this;
    }

    @JsonProperty("leave_timeout")
    public int getLeaveTimeout() {
        return this.leave_timeout;
    }

    @JsonProperty("leave_timeout")
    public GuildSettings setLeaveTimeout(int leaveTimeout) {
        this.leave_timeout = leaveTimeout;

        return this;
    }

    @JsonProperty("spam_threshold")
    public int getSpamThreshold() {
        return this.spam_threshold;
    }

    @JsonProperty("spam_threshold")
    public GuildSettings setSpamThreshold(int spamThreshold) {
        this.spam_threshold = spamThreshold;

        return this;
    }

    @JsonProperty("banLogging")
    public boolean isBanLogging() {
        return this.banLogging;
    }

    @JsonProperty("banLogging")
    public GuildSettings setBanLogging(boolean banLogging) {
        this.banLogging = banLogging;
        return this;
    }

    @JsonProperty("unbanLogging")
    public boolean isUnbanLogging() {
        return this.unbanLogging;
    }

    @JsonProperty("unbanLogging")
    public GuildSettings setUnbanLogging(boolean unbanLogging) {
        this.unbanLogging = unbanLogging;
        return this;
    }

    @JsonProperty("muteLogging")
    public boolean isMuteLogging() {
        return this.muteLogging;
    }

    @JsonProperty("muteLogging")
    public GuildSettings setMuteLogging(boolean muteLogging) {
        this.muteLogging = muteLogging;
        return this;
    }

    @JsonProperty("kickLogging")
    public boolean isKickLogging() {
        return this.kickLogging;
    }

    @JsonProperty("kickLogging")
    public GuildSettings setKickLogging(boolean kickLogging) {
        this.kickLogging = kickLogging;
        return this;
    }

    @JsonProperty("warnLogging")
    public boolean isWarnLogging() {
        return this.warnLogging;
    }

    @JsonProperty("warnLogging")
    public GuildSettings setWarnLogging(boolean warnLogging) {
        this.warnLogging = warnLogging;
        return this;
    }

    @JsonProperty("filterType")
    public ProfanityFilterType getFilterType() {
        return this.filterType;
    }

    @JsonIgnore
    public GuildSettings setFilterType(ProfanityFilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    @JsonProperty("aiSensitivity")
    public float getAiSensitivity() {
        return this.aiSensitivity;
    }

    @JsonProperty("aiSensitivity")
    public GuildSettings setAiSensitivity(float aiSensitivity) {
        this.aiSensitivity = aiSensitivity;
        return this;
    }

    @JsonProperty("allowAllToStop")
    public boolean isAllowAllToStop() {
        return this.allowAllToStop;
    }

    @JsonProperty("allowAllToStop")
    public GuildSettings setAllowAllToStop(boolean allowAllToStop) {
        this.allowAllToStop = allowAllToStop;
        return this;
    }

    @JsonProperty("warn_actions")
    public List<WarnAction> getWarnActions() {
        return this.warnActions;
    }

    @JsonProperty("warn_actions")
    public GuildSettings setWarnActions(List<WarnAction> warnings) {

        // if there are warn actions we can set them
        if (warnings.isEmpty()) {
            this.warnActions = List.of(new WarnAction(WarnAction.Type.KICK, 3));
        } else {
            this.warnActions = warnings;
        }

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
        return String.format("GuildSettings[%s]", guildId);
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
    public GuildSettings setRatelimits(JsonNode ratelimits) {
        this.ratelimits = ratelimmitChecks(ratelimits.asText());

        return this;
    }

    @JsonProperty("ratelimits")
    public String getRatelimitsDb() {
        return convertJ2S(this.ratelimits);
    }

    @JsonProperty("filterType")
    public GuildSettings setFilterType(String filterType) {
        this.filterType = ProfanityFilterType.fromType(filterType);
        return this;
    }

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
}
