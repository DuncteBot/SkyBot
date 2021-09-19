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

package ml.duncte123.skybot.objects.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MessageData {
    private final long messageId;
    private final long authorId;
    private final long channelId;
    private final String content;
    private final OffsetDateTime editedAt;

    public MessageData(
        long messageId, long authorId, long channelId, String content,
        OffsetDateTime editedAt
    ) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.editedAt = editedAt;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getMessageIdString() {
        return String.valueOf(messageId);
    }

    public long getAuthorId() {
        return authorId;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCratedAt() {
        return TimeUtil.getTimeCreated(this.messageId);
    }

    public boolean isEdit() {
        return this.getEditedAt() != null;
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
        map.put("edited_at", this.isEdit() ? this.editedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : "");

        return map;
    }

    public static MessageData from(Map<String, String> map) {
        return new MessageData(
            Long.parseLong(map.get("message_id")),
            Long.parseLong(map.get("author_id")),
            Long.parseLong(map.get("channel_id")),
            map.get("content"),
            map.get("edited_at").equals("") ? null : OffsetDateTime.parse(map.get("edited_at"))
        );
    }

    @Override
    public String toString() {
        return "MessageData{" +
            "messageId=" + messageId +
            ", authorId=" + authorId +
            ", channelId=" + channelId +
            ", content='" + content + '\'' +
            ", cratedAt=" + getCratedAt() +
            ", editedAt=" + editedAt +
            '}';
    }

    public static MessageData from(Message message) {
        return new MessageData(
            message.getIdLong(),
            message.getAuthor().getIdLong(),
            message.getChannel().getIdLong(),
            message.getContentRaw(),
            message.getTimeEdited()
        );
    }
}
