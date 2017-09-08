package ml.duncte123.skybot.objects.guild;

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
}
