/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.objects.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Fuck off :)
@SuppressWarnings({"PMD.NullAssignment", "PMD.UseConcurrentHashMap"})
public class MessageData {
    private final long messageId;
    private final long authorId;
    private final long channelId;
    private final String content;
    private final List<String> attachments;
    private final OffsetDateTime editedAt;

    public MessageData(
        long messageId, long authorId, long channelId, String content, List<String> attachments,
        @Nullable OffsetDateTime editedAt
    ) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.attachments = attachments;
        this.editedAt = editedAt;
    }

    public long getMessageId() {
        return this.messageId;
    }

    public String getMessageIdString() {
        return String.valueOf(this.messageId);
    }

    public long getAuthorId() {
        return this.authorId;
    }

    public long getChannelId() {
        return this.channelId;
    }

    public String getContent() {
        return this.content;
    }

    public OffsetDateTime getCratedAt() {
        return TimeUtil.getTimeCreated(this.messageId);
    }

    public List<String> getAttachments() {
        return this.attachments;
    }

    public boolean isEdit() {
        return this.getEditedAt() != null;
    }

    // TODO: using the param as failover for now, remove in the future when we can ensure the guild is stored
    // Although we don't actually have to store the guild id as we can get it from JDA
    public String getJumpUrl(long guildId) {
        return String.format("https://discord.com/channels/%s/%s/%s", guildId, this.channelId, this.messageId);
    }

    @Nullable
    public OffsetDateTime getEditedAt() {
        return editedAt;
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("message_id", String.valueOf(this.messageId));
        map.put("author_id", String.valueOf(this.authorId));
        map.put("channel_id", String.valueOf(this.channelId));
        map.put("content", this.content);
        map.put("edited_at", this.editedAt == null ? "" : this.editedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        map.put("attachment_count", String.valueOf(this.attachments.size()));

        for (int i = 0; i < this.attachments.size(); i++) {
            map.put("attachment_" + i, this.attachments.get(i));
        }

        return map;
    }

    public static MessageData from(Map<String, String> map) {
        final List<String> attachments;

        if (map.containsKey("attachment_count")) {
            final int size = Integer.parseInt(map.get("attachment_count"));
            attachments = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                attachments.add(i, map.get("attachment_" + i));
            }
        } else {
            attachments = Collections.emptyList();
        }

        return new MessageData(
            Long.parseLong(map.get("message_id")),
            Long.parseLong(map.get("author_id")),
            Long.parseLong(map.get("channel_id")),
            map.get("content"),
            attachments,
            "".equals(map.get("edited_at")) ? null : OffsetDateTime.parse(map.get("edited_at"))
        );
    }

    @Override
    public String toString() {
        return "MessageData{" +
            "messageId=" + this.messageId +
            ", authorId=" + this.authorId +
            ", channelId=" + this.channelId +
            ", content='" + this.content + '\'' +
            ", cratedAt=" + this.getCratedAt() +
            ", editedAt=" + this.editedAt +
            '}';
    }

    public static MessageData from(Message message) {
        return new MessageData(
            message.getIdLong(),
            message.getAuthor().getIdLong(),
            message.getChannel().getIdLong(),
            message.getContentRaw(),
            message.getAttachments().stream().map(Message.Attachment::getProxyUrl).toList(),
            message.getTimeEdited()
        );
    }
}
