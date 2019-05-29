/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.Author;

import javax.annotation.Nonnull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public interface ICommand {

    void executeCommand(@Nonnull CommandContext ctx);

    String getName();

    default String[] getAliases() {
        return new String[0];
    }

    String help(String prefix);

    default String help(String invoke, String prefix) {
        return help(prefix);
    }

    CommandCategory getCategory();

    default boolean isCustom() {
        return false;
    }

    boolean shouldDisplayAliasesInHelp();

}
