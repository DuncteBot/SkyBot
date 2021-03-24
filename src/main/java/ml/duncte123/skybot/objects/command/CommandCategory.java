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
    ANIMALS("animals"),
    MAIN("main"),
    FUN("fun"),
    MUSIC("music"),
    MODERATION("mod"),
    ADMINISTRATION("admin"),
    UTILS("utils"),
    PATRON("patron"),
    WEEB("weeb"),
    NSFW("nsfw"),
    LGBTQ("lgbtq+"),
    UNLISTED(null);

    private final String search;

    CommandCategory(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
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
