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

import java.util.HashMap;
import java.util.Map;

public class MessageData {
    private final long messageId;
    private final long authorId;
    private final String authorTag; // TODO: do we need this?
    private final String content;

    public MessageData(long messageId, long authorId, String authorTag, String content) {
        this.messageId = messageId;
        this.authorId = authorId;
        this.authorTag = authorTag;
        this.content = content;
    }

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("message_id", String.valueOf(this.messageId));
        map.put("author_id", String.valueOf(this.authorId));
        map.put("author_tag", this.authorTag);
        map.put("content", this.content);

        return map;
    }

    public static MessageData from(Map<String, String> map) {
        return new MessageData(
            Long.parseLong(map.get("message_id")),
            Long.parseLong(map.get("author_id")),
            map.get("author_tag"),
            map.get("content")
        );
    }

    @Override
    public String toString() {
        return "MessageData{" +
            "messageId=" + messageId +
            ", authorId=" + authorId +
            ", authorTag='" + authorTag + '\'' +
            ", content='" + content + '\'' +
            '}';
    }

    public static MessageData from(Message message) {
        return new MessageData(
            message.getIdLong(),
            message.getAuthor().getIdLong(),
            message.getAuthor().getAsTag(),
            message.getContentRaw()
        );
    }
}
