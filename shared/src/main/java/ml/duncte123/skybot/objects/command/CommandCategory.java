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

package ml.duncte123.skybot.objects.command;

public enum CommandCategory {
    ADMINISTRATION("admin", "Administration"),
    ANIMALS("animals", "Animal"),
    FUN("fun", "Fun"),
    LGBTQ("lgbtq+", "LGBTQ+"),
    MAIN("main", "Uncategorized"),
    MODERATION("mod", "Moderation"),
    MUSIC("music", "Music"),
    NSFW("nsfw", "NSFW"),
    PATRON("patron", "Patron only"),
    UTILS("utils", "Utility"),
    WEEB("weeb", "Weeb"),
    // Unlisted is always last
    UNLISTED(null, null);

    private final String search;
    private final String display;

    CommandCategory(String search, String display) {
        this.search = search;
        this.display = display;
    }

    public String getSearch() {
        return search;
    }

    public String getDisplay() {
        return display;
    }

    public static CommandCategory fromSearch(String input) {
        for (final CommandCategory value : values()) {
            if (input.equalsIgnoreCase(value.name()) || input.equalsIgnoreCase(value.getSearch())) {
                return value;
            }
        }

        return null;
    }
}
