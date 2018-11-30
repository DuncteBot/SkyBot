/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.command.custom;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.ICommand;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public interface CustomCommand extends ICommand {

    String getMessage();

    long getGuildId();

    @Override
    default boolean isCustom() {
        return true;
    }

    //Override some methods that are not needed
    @Override
    default void executeCommand(@NotNull CommandContext ctx) {
        // Custom commands are executed in a different way
    }

    @Override
    default String help() {
        return null;
    }

    @Override
    default CommandCategory getCategory() {
        return null;
    }

    @Override
    default boolean shouldDisplayAliasesInHelp() {
        return false;
    }

    default JSONObject toJSONObject() {
        return new JSONObject()
            .put("guildId", getGuildId())
            .put("name", getName())
            .put("message", getMessage());
    }
}
