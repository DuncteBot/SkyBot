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

package ml.duncte123.skybot.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("PMD")
public class Tag {
    private final int id;
    public final String name;
    public final String content;
    public final long ownerId;

    public Tag(int id, String name, String content, long ownerId) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.ownerId = ownerId;
    }

    @JsonCreator
    public Tag(@JsonProperty("name") String name, @JsonProperty("content") String content, @JsonProperty("owner_id") long ownerId) {
        this(0, name, content, ownerId);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public long getOwnerId() {
        return ownerId;
    }
}
