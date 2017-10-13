package ml.duncte123.skybot.commands.essentials;

import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import javax.annotation.Nullable;
import java.util.List;

public class FakeGuild implements Guild {
    @Override
    public String getName() {
        return "duncte123";
    }

    @Override
    public String getIconId() {
        return null;
    }

    @Override
    public String getIconUrl() {
        return null;
    }

    @Override
    public String getSplashId() {
        return null;
    }

    @Override
    public String getSplashUrl() {
        return null;
    }

    @Override
    public VoiceChannel getAfkChannel() {
        return null;
    }

    @Override
    public TextChannel getSystemChannel() {
        return null;
    }

    @Override
    public Member getOwner() {
        return null;
    }

    @Override
    public Timeout getAfkTimeout() {
        return null;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public boolean isMember(User user) {
        return false;
    }

    @Override
    public Member getSelfMember() {
        return null;
    }

    @Override
    public Member getMember(User user) {
        return new FakeMember();
    }

    @Override
    public MemberCacheView getMemberCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        return null;
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache() {
        return null;
    }

    @Override
    public RestAction<List<User>> getBans() {
        return null;
    }

    @Override
    public RestAction<Integer> getPrunableMemberCount(int days) {
        return null;
    }

    @Override
    public Role getPublicRole() {
        return null;
    }

    @Override
    public TextChannel getPublicChannel() {
        return null;
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel() {
        return null;
    }

    @Override
    public GuildManager getManager() {
        return null;
    }

    @Override
    public GuildManagerUpdatable getManagerUpdatable() {
        return null;
    }

    @Override
    public GuildController getController() {
        return null;
    }

    @Override
    public MentionPaginationAction getRecentMentions() {
        return null;
    }

    @Override
    public AuditLogPaginationAction getAuditLogs() {
        return null;
    }

    @Override
    public RestAction<Void> leave() {
        return null;
    }

    @Override
    public RestAction<Void> delete() {
        return null;
    }

    @Override
    public RestAction<Void> delete(String mfaCode) {
        return null;
    }

    @Override
    public AudioManager getAudioManager() {
        return null;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public RestAction<List<Invite>> getInvites() {
        return null;
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks() {
        return null;
    }

    @Override
    public List<GuildVoiceState> getVoiceStates() {
        return null;
    }

    @Override
    public VerificationLevel getVerificationLevel() {
        return null;
    }

    @Override
    public NotificationLevel getDefaultNotificationLevel() {
        return null;
    }

    @Override
    public MFALevel getRequiredMFALevel() {
        return null;
    }

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
        return true;
    }

    @Override
    public String getId() {
        return getIdLong() + "";
    }

    @Override
    public long getIdLong() {
        return 191245668617158656L;
    }
}
