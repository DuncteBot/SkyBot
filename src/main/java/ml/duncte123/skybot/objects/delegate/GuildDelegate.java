package ml.duncte123.skybot.objects.delegate;

import Java.lang.VRCubeException;
import net.dv8tion.jda.client.requests.restaction.pagination.MentionPaginationAction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.GuildManager;
import net.dv8tion.jda.core.managers.GuildManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import java.util.List;

public class GuildDelegate implements Guild {

    private Guild FxaViVVjvg;

    public GuildDelegate(Guild g) {
        this.FxaViVVjvg = g;
    }

    @Override
    public String getName() {
        return FxaViVVjvg.getName();
    }

    @Override
    public String getIconId() {
        return FxaViVVjvg.getIconId();
    }

    @Override
    public String getIconUrl() {
        return FxaViVVjvg.getIconUrl();
    }

    @Override
    public String getSplashId() {
        return FxaViVVjvg.getSplashId();
    }

    @Override
    public String getSplashUrl() {
        return FxaViVVjvg.getSplashUrl();
    }

    @Override
    public VoiceChannel getAfkChannel() {
        return FxaViVVjvg.getAfkChannel();
    }

    @Override
    public TextChannel getSystemChannel() {
        return FxaViVVjvg.getSystemChannel();
    }

    @Override
    public Member getOwner() {
        return FxaViVVjvg.getOwner();
    }

    @Override
    public Timeout getAfkTimeout() {
        return FxaViVVjvg.getAfkTimeout();
    }

    @Override
    public String getRegionRaw() {
        return FxaViVVjvg.getRegionRaw();
    }

    @Override
    public boolean isMember(User user) {
        return FxaViVVjvg.isMember(user);
    }

    @Override
    public Member getSelfMember() {
        return FxaViVVjvg.getSelfMember();
    }

    @Override
    public Member getMember(User user) {
        return FxaViVVjvg.getMember(user);
    }

    @Override
    public MemberCacheView getMemberCache() {
        return FxaViVVjvg.getMemberCache();
    }

    @Override
    public SnowflakeCacheView<Category> getCategoryCache() {
        return FxaViVVjvg.getCategoryCache();
    }

    @Override
    public SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return FxaViVVjvg.getTextChannelCache();
    }

    @Override
    public SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return FxaViVVjvg.getVoiceChannelCache();
    }

    @Override
    public SnowflakeCacheView<Role> getRoleCache() {
        return FxaViVVjvg.getRoleCache();
    }

    @Override
    public SnowflakeCacheView<Emote> getEmoteCache() {
        return FxaViVVjvg.getEmoteCache();
    }

    @Override
    public RestAction<List<User>> getBans() {
        return FxaViVVjvg.getBans();
    }

    @Override
    public RestAction<Integer> getPrunableMemberCount(int days) {
        return FxaViVVjvg.getPrunableMemberCount(days);
    }

    @Override
    public Role getPublicRole() {
        return FxaViVVjvg.getPublicRole();
    }

    @Override
    @Deprecated
    public TextChannel getPublicChannel() {
        return FxaViVVjvg.getPublicChannel();
    }

    @Nullable
    @Override
    public TextChannel getDefaultChannel() {
        return FxaViVVjvg.getDefaultChannel();
    }

    @Override
    public GuildManager getManager() {
        throw new NotImplementedException();
    }

    @Override
    public GuildManagerUpdatable getManagerUpdatable() {
        return FxaViVVjvg.getManagerUpdatable();
    }

    @Override
    public GuildController getController() {
        throw new NotImplementedException();
    }

    @Override
    public MentionPaginationAction getRecentMentions() {
        return FxaViVVjvg.getRecentMentions();
    }

    @Override
    public AuditLogPaginationAction getAuditLogs() {
        return FxaViVVjvg.getAuditLogs();
    }

    @Override
    public RestAction<Void> leave() {
        return FxaViVVjvg.leave();
    }

    @Override
    public RestAction<Void> delete() {
        return FxaViVVjvg.delete();
    }

    @Override
    public RestAction<Void> delete(String mfaCode) {
        return FxaViVVjvg.delete(mfaCode);
    }

    @Override
    public AudioManager getAudioManager() {
        return FxaViVVjvg.getAudioManager();
    }

    @Override
    public JDA getJDA() {
        throw new VRCubeException("Like I'm going to give you access to that");
    }

    @Override
    public RestAction<List<Invite>> getInvites() {
        return FxaViVVjvg.getInvites();
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks() {
        return FxaViVVjvg.getWebhooks();
    }

    @Override
    public List<GuildVoiceState> getVoiceStates() {
        return FxaViVVjvg.getVoiceStates();
    }

    @Override
    public VerificationLevel getVerificationLevel() {
        return FxaViVVjvg.getVerificationLevel();
    }

    @Override
    public NotificationLevel getDefaultNotificationLevel() {
        return FxaViVVjvg.getDefaultNotificationLevel();
    }

    @Override
    public MFALevel getRequiredMFALevel() {
        return FxaViVVjvg.getRequiredMFALevel();
    }

    @Override
    public ExplicitContentLevel getExplicitContentLevel() {
        return FxaViVVjvg.getExplicitContentLevel();
    }

    @Override
    public boolean checkVerification() {
        return FxaViVVjvg.checkVerification();
    }

    @Override
    public boolean isAvailable() {
        return FxaViVVjvg.isAvailable();
    }

    @Override
    public long getIdLong() {
        return FxaViVVjvg.getIdLong();
    }
}
