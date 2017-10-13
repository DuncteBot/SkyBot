package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.ChannelManager;
import net.dv8tion.jda.core.managers.ChannelManagerUpdatable;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.*;

import java.util.Collection;
import java.util.List;

public class FakeTextChannel implements TextChannel {
    @Override
    public String getTopic() {
        return null;
    }

    @Override
    public boolean isNSFW() {
        return false;
    }

    @Override
    public RestAction<List<Webhook>> getWebhooks() {
        return null;
    }

    @Override
    public WebhookAction createWebhook(String name) {
        return null;
    }

    @Override
    public RestAction<Void> deleteMessages(Collection<Message> messages) {
        return null;
    }

    @Override
    public RestAction<Void> deleteMessagesByIds(Collection<String> messageIds) {
        return null;
    }

    @Override
    public AuditableRestAction<Void> deleteWebhookById(String id) {
        return null;
    }

    @Override
    public RestAction<Void> clearReactionsById(String messageId) {
        return null;
    }

    @Override
    public boolean canTalk() {
        return true;
    }

    @Override
    public boolean canTalk(Member member) {
        return false;
    }

    @Override
    public int compareTo(TextChannel o) {
        return 0;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.TEXT;
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
    public String getName() {
        return "test_channel";
    }

    @Override
    public Guild getGuild() {
        return new FakeGuild();
    }

    @Override
    public Category getParent() {
        return null;
    }

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

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public PermissionOverride getPermissionOverride(Member member) {
        return null;
    }

    @Override
    public PermissionOverride getPermissionOverride(Role role) {
        return null;
    }

    @Override
    public List<PermissionOverride> getPermissionOverrides() {
        return null;
    }

    @Override
    public List<PermissionOverride> getMemberPermissionOverrides() {
        return null;
    }

    @Override
    public List<PermissionOverride> getRolePermissionOverrides() {
        return null;
    }

    @Override
    public ChannelAction createCopy(Guild guild) {
        return null;
    }

    @Override
    public ChannelManager getManager() {
        return null;
    }

    @Override
    public ChannelManagerUpdatable getManagerUpdatable() {
        return null;
    }

    @Override
    public AuditableRestAction<Void> delete() {
        return null;
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Member member) {
        return null;
    }

    @Override
    public PermissionOverrideAction createPermissionOverride(Role role) {
        return null;
    }

    @Override
    public InviteAction createInvite() {
        return null;
    }

    @Override
    public RestAction<List<Invite>> getInvites() {
        return null;
    }

    @Override
    public String getAsMention() {
        return "<#" + getIdLong() + ">";
    }

    @Override
    public long getIdLong() {
        return AirUtils.rand.nextLong();
    }
}
