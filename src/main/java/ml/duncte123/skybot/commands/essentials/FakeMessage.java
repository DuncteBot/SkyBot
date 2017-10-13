package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import java.time.OffsetDateTime;
import java.util.Formatter;
import java.util.List;

public class FakeMessage implements Message {
    @Override
    public List<User> getMentionedUsers() {
        return null;
    }

    @Override
    public boolean isMentioned(User user) {
        return false;
    }

    @Override
    public List<TextChannel> getMentionedChannels() {
        return null;
    }

    @Override
    public List<Role> getMentionedRoles() {
        return null;
    }

    @Override
    public boolean mentionsEveryone() {
        return false;
    }

    @Override
    public boolean isEdited() {
        return false;
    }

    @Override
    public OffsetDateTime getEditedTime() {
        return null;
    }

    @Override
    public User getAuthor() {
        return new FakeUser();
    }

    @Override
    public Member getMember() {
        return new FakeMember();
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public String getRawContent() {
        return "/eval while(true) System.out.println(System.currentTimeMillis());";
    }

    @Override
    public String getStrippedContent() {
        return null;
    }

    @Override
    public boolean isFromType(ChannelType type) {
        return false;
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.TEXT;
    }

    @Override
    public boolean isWebhookMessage() {
        return false;
    }

    @Override
    public MessageChannel getChannel() {
        return new FakeTextChannel();
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
        return new FakeTextChannel();
    }

    @Override
    public Category getCategory() {
        return null;
    }

    @Override
    public Guild getGuild() {
        return new FakeGuild();
    }

    @Override
    public List<Attachment> getAttachments() {
        return null;
    }

    @Override
    public List<MessageEmbed> getEmbeds() {
        return null;
    }

    @Override
    public List<Emote> getEmotes() {
        return null;
    }

    @Override
    public List<MessageReaction> getReactions() {
        return null;
    }

    @Override
    public boolean isTTS() {
        return false;
    }

    @Override
    public RestAction<Message> editMessage(String newContent) {
        return null;
    }

    @Override
    public RestAction<Message> editMessage(MessageEmbed newContent) {
        return null;
    }

    @Override
    public RestAction<Message> editMessageFormat(String format, Object... args) {
        return null;
    }

    @Override
    public RestAction<Message> editMessage(Message newContent) {
        return null;
    }

    @Override
    public AuditableRestAction<Void> delete() {
        return null;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public boolean isPinned() {
        return false;
    }

    @Override
    public RestAction<Void> pin() {
        return null;
    }

    @Override
    public RestAction<Void> unpin() {
        return null;
    }

    @Override
    public RestAction<Void> addReaction(Emote emote) {
        return null;
    }

    @Override
    public RestAction<Void> addReaction(String unicode) {
        return null;
    }

    @Override
    public RestAction<Void> clearReactions() {
        return null;
    }

    @Override
    public MessageType getType() {
        return null;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {

    }

    @Override
    public long getIdLong() {
        return AirUtils.rand.nextLong();
    }
}
