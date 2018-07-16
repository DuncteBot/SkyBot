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

package ml.duncte123.skybot.command;

import ml.duncte123.skybot.Settings;
import net.dv8tion.jda.bot.JDABot;
import net.dv8tion.jda.client.JDAClient;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.managers.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.*;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.cache.CacheView;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;

@SuppressWarnings("ConstantConditions")
class FakeGuildMessageReceivedEvent extends GuildMessageReceivedEvent {

    FakeGuildMessageReceivedEvent(DummyCommand cmd) {
        super(new JDA() {
            @Nonnull
            @Override
            public Status getStatus() {
                return null;
            }

            @Override
            public long getPing() {
                return 0;
            }

            @Nonnull
            @Override
            public List<String> getCloudflareRays() {
                return null;
            }

            @Nonnull
            @Override
            public List<String> getWebSocketTrace() {
                return null;
            }

            @Override
            public void setEventManager(IEventManager manager) {

            }

            @Override
            public void addEventListener(Object... listeners) {

            }

            @Override
            public void removeEventListener(Object... listeners) {

            }

            @Nonnull
            @Override
            public List<Object> getRegisteredListeners() {
                return null;
            }

            @Nonnull
            @Override
            public GuildAction createGuild(String name) {
                return null;
            }

            @Nonnull
            @Override
            public CacheView<AudioManager> getAudioManagerCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<User> getUserCache() {
                return null;
            }

            @Nonnull
            @Override
            public List<Guild> getMutualGuilds(User... users) {
                return null;
            }

            @Nonnull
            @Override
            public List<Guild> getMutualGuilds(Collection<User> users) {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<User> retrieveUserById(String id) {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<User> retrieveUserById(long id) {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<Guild> getGuildCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<Role> getRoleCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<Category> getCategoryCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<TextChannel> getTextChannelCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
                return null;
            }

            @Nonnull
            @Override
            public SnowflakeCacheView<Emote> getEmoteCache() {
                return null;
            }

            @Nonnull
            @Override
            public SelfUser getSelfUser() {
                return null;
            }

            @Nonnull
            @Override
            public Presence getPresence() {
                return null;
            }

            @Override
            public ShardInfo getShardInfo() {
                return null;
            }

            @Nonnull
            @Override
            public String getToken() {
                return null;
            }

            @Override
            public long getResponseTotal() {
                return 0;
            }

            @Override
            public int getMaxReconnectDelay() {
                return 0;
            }

            @Override
            public void setRequestTimeoutRetry(boolean retryOnTimeout) {

            }

            @Override
            public boolean isAutoReconnect() {
                return false;
            }

            @Override
            public void setAutoReconnect(boolean reconnect) {

            }

            @Override
            public boolean isAudioEnabled() {
                return false;
            }

            @Override
            public boolean isBulkDeleteSplittingEnabled() {
                return false;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public void shutdownNow() {

            }

            @Nonnull
            @Override
            public AccountType getAccountType() {
                return null;
            }

            @Nonnull
            @Override
            public JDAClient asClient() {
                return null;
            }

            @Nonnull
            @Override
            public JDABot asBot() {
                return null;
            }
        }, 0L, new Message() {
            @Nonnull
            @Override
            public List<User> getMentionedUsers() {
                return null;
            }

            @Nonnull
            @Override
            public List<TextChannel> getMentionedChannels() {
                return null;
            }

            @Nonnull
            @Override
            public List<Role> getMentionedRoles() {
                return null;
            }

            @Nonnull
            @Override
            public List<Member> getMentionedMembers(Guild guild) {
                return null;
            }

            @Nonnull
            @Override
            public List<Member> getMentionedMembers() {
                return null;
            }

            @Nonnull
            @Override
            public List<IMentionable> getMentions(MentionType... types) {
                return null;
            }

            @Override
            public boolean isMentioned(IMentionable mentionable, MentionType... types) {
                return false;
            }

            @Override
            public boolean mentionsEveryone() {
                return false;
            }

            @Override
            public boolean isEdited() {
                return false;
            }

            @Nullable
            @Override
            public OffsetDateTime getEditedTime() {
                return null;
            }

            @Nonnull
            @Override
            public User getAuthor() {
                return null;
            }

            @Nullable
            @Override
            public Member getMember() {
                return null;
            }

            @Nonnull
            @Override
            public String getContentDisplay() {
                return Settings.PREFIX + cmd.getName() + " bla bla bla";
            }

            @Nonnull
            @Override
            public String getContentRaw() {
                return Settings.PREFIX + cmd.getName() + " bla bla bla";
            }

            @Nonnull
            @Override
            public String getContentStripped() {
                return Settings.PREFIX + cmd.getName() + " bla bla bla";
            }

            @Nonnull
            @Override
            public List<String> getInvites() {
                return null;
            }

            @Nullable
            @Override
            public String getNonce() {
                return null;
            }

            @Override
            public boolean isFromType(ChannelType type) {
                return false;
            }

            @Nonnull
            @Override
            public ChannelType getChannelType() {
                return null;
            }

            @Override
            public boolean isWebhookMessage() {
                return false;
            }

            @Nonnull
            @Override
            public MessageChannel getChannel() {
                return null;
            }

            @Override
            public PrivateChannel getPrivateChannel() {
                return null;
            }

            @Override
            public Group getGroup() {
                return null;
            }

            @Override
            public TextChannel getTextChannel() {
                return new TextChannel() {
                    @Nullable
                    @Override
                    public String getTopic() {
                        return null;
                    }

                    @Override
                    public boolean isNSFW() {
                        return false;
                    }

                    @Nonnull
                    @Override
                    public RestAction<List<Webhook>> getWebhooks() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public WebhookAction createWebhook(String name) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public RestAction<Void> deleteMessages(Collection<Message> messages) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public RestAction<Void> deleteMessagesByIds(Collection<String> messageIds) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public AuditableRestAction<Void> deleteWebhookById(String id) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public RestAction<Void> clearReactionsById(String messageId) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public RestAction<Void> removeReactionById(String messageId, String unicode, User user) {
                        return null;
                    }

                    @Override
                    public boolean canTalk() {
                        return false;
                    }

                    @Override
                    public boolean canTalk(Member member) {
                        return false;
                    }

                    @Override
                    public int compareTo(@NotNull TextChannel o) {
                        return 0;
                    }

                    @Nonnull
                    @Override
                    public ChannelType getType() {
                        return ChannelType.TEXT;
                    }

                    @Nonnull
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public Guild getGuild() {
                        return new Guild() {
                            @Override
                            public RestAction<EnumSet<Region>> retrieveRegions() {
                                return null;
                            }

                            @Override
                            public RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated) {
                                return null;
                            }

                            @Override
                            public MemberAction addMember(String accessToken, String userId) {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public String getName() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public String getIconId() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public String getIconUrl() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public Set<String> getFeatures() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public String getSplashId() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public String getSplashUrl() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<String> getVanityUrl() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public VoiceChannel getAfkChannel() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public TextChannel getSystemChannel() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public Member getOwner() {
                                return null;
                            }

                            @Override
                            public long getOwnerIdLong() {
                                return 0;
                            }

                            @Nonnull
                            @Override
                            public Timeout getAfkTimeout() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public String getRegionRaw() {
                                return null;
                            }

                            @Override
                            public boolean isMember(User user) {
                                return false;
                            }

                            @Nonnull
                            @Override
                            public Member getSelfMember() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public Member getMember(User user) {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public MemberCacheView getMemberCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public SnowflakeCacheView<Category> getCategoryCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public SnowflakeCacheView<TextChannel> getTextChannelCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public SnowflakeCacheView<Role> getRoleCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public SnowflakeCacheView<Emote> getEmoteCache() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<List<Ban>> getBanList() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<Integer> getPrunableMemberCount(int days) {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public Role getPublicRole() {
                                return null;
                            }

                            @Nullable
                            @Override
                            public TextChannel getDefaultChannel() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public GuildManager getManager() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public GuildController getController() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public MentionPaginationAction getRecentMentions() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public AuditLogPaginationAction getAuditLogs() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<Void> leave() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<Void> delete() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<Void> delete(@Nullable String mfaCode) {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public AudioManager getAudioManager() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public JDA getJDA() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<List<Invite>> getInvites() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public RestAction<List<Webhook>> getWebhooks() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public List<GuildVoiceState> getVoiceStates() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public VerificationLevel getVerificationLevel() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public NotificationLevel getDefaultNotificationLevel() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public MFALevel getRequiredMFALevel() {
                                return null;
                            }

                            @Nonnull
                            @Override
                            public ExplicitContentLevel getExplicitContentLevel() {
                                return null;
                            }

                            @Override
                            public boolean checkVerification() {
                                return false;
                            }

                            @Override
                            public boolean isAvailable() {
                                return false;
                            }

                            @Override
                            public long getIdLong() {
                                return 0;
                            }
                        };
                    }

                    @Nullable
                    @Override
                    public Category getParent() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public List<Member> getMembers() {
                        return null;
                    }

                    @Override
                    public int getPosition() {
                        return 0;
                    }

                    @Override
                    public int getPositionRaw() {
                        return 0;
                    }

                    @Nonnull
                    @Override
                    public JDA getJDA() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public PermissionOverride getPermissionOverride(Member member) {
                        return null;
                    }

                    @Nullable
                    @Override
                    public PermissionOverride getPermissionOverride(Role role) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public List<PermissionOverride> getPermissionOverrides() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public List<PermissionOverride> getMemberPermissionOverrides() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public List<PermissionOverride> getRolePermissionOverrides() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public ChannelAction createCopy(Guild guild) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public ChannelManager getManager() {
                        return null;
                    }


                    @Nonnull
                    @Override
                    public AuditableRestAction<Void> delete() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public PermissionOverrideAction createPermissionOverride(Member member) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public PermissionOverrideAction createPermissionOverride(Role role) {
                        return null;
                    }

                    @Override
                    public PermissionOverrideAction putPermissionOverride(Member member) {
                        return null;
                    }

                    @Override
                    public PermissionOverrideAction putPermissionOverride(Role role) {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public InviteAction createInvite() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public RestAction<List<Invite>> getInvites() {
                        return null;
                    }

                    @Nonnull
                    @Override
                    public String getAsMention() {
                        return null;
                    }

                    @Override
                    public long getLatestMessageIdLong() {
                        return 0;
                    }

                    @Override
                    public boolean hasLatestMessage() {
                        return false;
                    }

                    @Override
                    public long getIdLong() {
                        return 0;
                    }
                };
            }

            @Nullable
            @Override
            public Category getCategory() {
                return null;
            }

            @Override
            public Guild getGuild() {
                return null;
            }

            @Nonnull
            @Override
            public List<Attachment> getAttachments() {
                return null;
            }

            @Nonnull
            @Override
            public List<MessageEmbed> getEmbeds() {
                return null;
            }

            @Nonnull
            @Override
            public List<Emote> getEmotes() {
                return null;
            }

            @Nonnull
            @Override
            public List<MessageReaction> getReactions() {
                return null;
            }

            @Override
            public boolean isTTS() {
                return false;
            }

            @Nonnull
            @Override
            public MessageAction editMessage(CharSequence newContent) {
                return null;
            }

            @Nonnull
            @Override
            public MessageAction editMessage(MessageEmbed newContent) {
                return null;
            }

            @Nonnull
            @Override
            public MessageAction editMessageFormat(String format, Object... args) {
                return null;
            }

            @Nonnull
            @Override
            public MessageAction editMessage(Message newContent) {
                return null;
            }

            @Nonnull
            @Override
            public AuditableRestAction<Void> delete() {
                return null;
            }

            @Nonnull
            @Override
            public JDA getJDA() {
                return null;
            }

            @Override
            public boolean isPinned() {
                return false;
            }

            @Nonnull
            @Override
            public RestAction<Void> pin() {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<Void> unpin() {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<Void> addReaction(Emote emote) {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<Void> addReaction(String unicode) {
                return null;
            }

            @Nonnull
            @Override
            public RestAction<Void> clearReactions() {
                return null;
            }

            @Nonnull
            @Override
            public MessageType getType() {
                return null;
            }

            @Override
            public void formatTo(Formatter formatter, int flags, int width, int precision) {

            }

            @Override
            public long getIdLong() {
                return 0;
            }
        });
    }
}
