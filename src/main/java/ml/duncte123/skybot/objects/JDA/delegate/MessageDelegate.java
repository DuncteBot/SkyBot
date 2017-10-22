/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.objects.JDA.delegate;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;

import java.time.OffsetDateTime;
import java.util.Formatter;
import java.util.List;

public class MessageDelegate implements Message {

    private Message t1hVSePqE6;

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        t1hVSePqE6.formatTo(formatter, flags, width, precision);
    }

    public MessageDelegate(Message t1hVSePqE6) {
        this.t1hVSePqE6 = t1hVSePqE6;
    }

    @Override
    public List<User> getMentionedUsers() {
        return t1hVSePqE6.getMentionedUsers();
    }

    @Override
    public boolean isMentioned(User user) {
        return t1hVSePqE6.isMentioned(user);
    }

    @Override
    public List<TextChannel> getMentionedChannels() {
        return t1hVSePqE6.getMentionedChannels();
    }

    @Override
    public List<Role> getMentionedRoles() {
        return t1hVSePqE6.getMentionedRoles();
    }

    @Override
    public boolean mentionsEveryone() {
        return t1hVSePqE6.mentionsEveryone();
    }

    @Override
    public boolean isEdited() {
        return t1hVSePqE6.isEdited();
    }

    @Override
    public OffsetDateTime getEditedTime() {
        return t1hVSePqE6.getEditedTime();
    }

    @Override
    public User getAuthor() {
        return t1hVSePqE6.getAuthor();
    }

    @Override
    public Member getMember() {
        return t1hVSePqE6.getMember();
    }

    @Override
    public String getContent() {
        return t1hVSePqE6.getContent();
    }

    @Override
    public String getRawContent() {
        return t1hVSePqE6.getRawContent();
    }

    @Override
    public String getStrippedContent() {
        return t1hVSePqE6.getStrippedContent();
    }

    @Override
    public boolean isFromType(ChannelType type) {
        return t1hVSePqE6.isFromType(type);
    }

    @Override
    public ChannelType getChannelType() {
        return t1hVSePqE6.getChannelType();
    }

    @Override
    public boolean isWebhookMessage() {
        return t1hVSePqE6.isWebhookMessage();
    }

    @Override
    public MessageChannel getChannel() {
        return t1hVSePqE6.getChannel();
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        return t1hVSePqE6.getPrivateChannel();
    }

    @Override
    public Group getGroup() {
        return t1hVSePqE6.getGroup();
    }

    @Override
    public TextChannel getTextChannel() {
        return t1hVSePqE6.getTextChannel();
    }

    @Override
    public Category getCategory() {
        return t1hVSePqE6.getCategory();
    }

    @Override
    public Guild getGuild() {
        return t1hVSePqE6.getGuild();
    }

    @Override
    public List<Attachment> getAttachments() {
        return t1hVSePqE6.getAttachments();
    }

    @Override
    public List<MessageEmbed> getEmbeds() {
        return t1hVSePqE6.getEmbeds();
    }

    @Override
    public List<Emote> getEmotes() {
        return t1hVSePqE6.getEmotes();
    }

    @Override
    public List<MessageReaction> getReactions() {
        return t1hVSePqE6.getReactions();
    }

    @Override
    public boolean isTTS() {
        return t1hVSePqE6.isTTS();
    }

    @Override
    public RestAction<Message> editMessage(String newContent) {
        return t1hVSePqE6.editMessage(newContent);
    }

    @Override
    public RestAction<Message> editMessage(MessageEmbed newContent) {
        return t1hVSePqE6.editMessage(newContent);
    }

    @Override
    public RestAction<Message> editMessageFormat(String format, Object... args) {
        return t1hVSePqE6.editMessageFormat(format, args);
    }

    @Override
    public RestAction<Message> editMessage(Message newContent) {
        return t1hVSePqE6.editMessage(newContent);
    }

    @Override
    public AuditableRestAction<Void> delete() {
        return t1hVSePqE6.delete();
    }

    @Override
    public JDA getJDA() {
        return new JDADelegate(t1hVSePqE6.getJDA());
    }

    @Override
    public boolean isPinned() {
        return t1hVSePqE6.isPinned();
    }

    @Override
    public RestAction<Void> pin() {
        return t1hVSePqE6.pin();
    }

    @Override
    public RestAction<Void> unpin() {
        return t1hVSePqE6.unpin();
    }

    @Override
    public RestAction<Void> addReaction(Emote emote) {
        return t1hVSePqE6.addReaction(emote);
    }

    @Override
    public RestAction<Void> addReaction(String unicode) {
        return t1hVSePqE6.addReaction(unicode);
    }

    @Override
    public RestAction<Void> clearReactions() {
        return t1hVSePqE6.clearReactions();
    }

    @Override
    public MessageType getType() {
        return t1hVSePqE6.getType();
    }

    @Override
    public String getId() {
        return t1hVSePqE6.getId();
    }

    @Override
    public OffsetDateTime getCreationTime() {
        return t1hVSePqE6.getCreationTime();
    }

    @Override
    public long getIdLong() {
        return t1hVSePqE6.getIdLong();
    }
}
