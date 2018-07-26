/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.objects.Tag;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class TagUtils {
    /**
     * This stores all the tags
     */
    public static Map<String, Tag> tagsList = new TreeMap<>();
    private static Logger logger = LoggerFactory.getLogger(TagUtils.class);

    /**
     * Attempts to load all the tags from the database
     */
    public static void loadAllTags() {
        Variables.DATABASE.run(() -> {
            logger.debug("Loading tags.");

            Connection database = Variables.DATABASE.getConnManager().getConnection();
            try {
                Statement smt = database.createStatement();

                ResultSet resultSet = smt.executeQuery("SELECT * FROM " + Variables.DATABASE.getName() + ".tags");

                while (resultSet.next()) {
                    String tagName = resultSet.getString("tagName");

                    tagsList.put(tagName, new Tag(
                            resultSet.getInt("id"),
                            resultSet.getString("author"),
                            resultSet.getString("authorId"),
                            tagName,
                            resultSet.getString("tagText")
                    ));
                }

                logger.debug("Loaded " + tagsList.keySet().size() + " tags.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    database.close();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        });
    }

    /**
     * Attempts to register a new tag
     *
     * @param author The user that created the tag
     * @param tag    the {@link Tag} to add
     * @return True if the tag is added
     */
    public static boolean registerNewTag(User author, Tag tag) {
        if (tagsList.containsKey(tag.getName())) //Return false if the tag is already here
            return false;

        Connection database = Variables.DATABASE.getConnManager().getConnection();

        try {
            PreparedStatement statement = database.prepareStatement("INSERT INTO " + Variables.DATABASE.getName() + ".tags(author ,authorId ,tagName ,tagText) " +
                    "VALUES(? , ? , ? , ?)");
            statement.setString(1, String.format("%#s", author));
            statement.setString(2, author.getId());
            statement.setString(3, tag.getName());
            statement.setString(4, tag.getText());
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                database.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }

        tagsList.put(tag.getName(), tag);
        return true;
    }

    /**
     * Attempts to delete a tag
     *
     * @param tag the {@link Tag} to delete
     * @return true if the tag is deleted
     */
    public static boolean deleteTag(Tag tag) {
        try {
            return Variables.DATABASE.run(() -> {
                Connection database = Variables.DATABASE.getConnManager().getConnection();

                try {
                    PreparedStatement statement = database.prepareStatement("DELETE FROM " + Variables.DATABASE.getName() + ".tags WHERE tagName= ? ");
                    statement.setString(1, tag.getName());
                    statement.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        tagsList.remove(tag.getName());
                        database.close();
                        return true;
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                }
                return false;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
