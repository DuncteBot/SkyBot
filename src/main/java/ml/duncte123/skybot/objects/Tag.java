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

package ml.duncte123.skybot.objects;

import org.apache.commons.lang3.StringUtils;

/**
 * @author duncte123
 *
 * This is the model that all the tags use
 */
public class Tag {

    private int tagId;
    private String author;
    private String authorId;
    private String name;
    private String text;

    /**
     * The tag constructor
     * @param author Who made the tag? Username#discrim
     * @param name What is the tag called
     * @param text What to put into the tag
     */
    public Tag(int tagId, String author, String authorId, String name, String text) {
        this.tagId = tagId;
        this.author = author;
        this.authorId = authorId;
        this.name = name;
        this.text = text;
    }

    /**
     * Returns the tag name
     * @return the tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns who created the tag
     * @return who created the tag
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the user id of the author
     * @return the user id of the author
     */
    public String getAuthorId() {
        return authorId;
    }

    /**
     * Returns the contends of the tag
     * @return the contends of the tag
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the tag id
     * @return the tag id
     */
    public int getTagId() {
        return tagId;
    }

    @Override
    public String toString() {
        return String.format("Tag[%s, %s, Author: %s]", name, StringUtils.abbreviate(text, 30), author);
    }
}
