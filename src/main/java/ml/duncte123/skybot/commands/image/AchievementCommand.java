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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class AchievementCommand extends ImageCommandBase {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (!hasArgs(ctx.getEvent(), ctx.getArgs())) return;
        if (!canSendFile(ctx.getEvent())) return;

        ctx.getAlexFlipnote().getAchievement(parseTextArgsForImagae(ctx))
                .async((image) -> handleBasicImage(ctx.getEvent(), image));
    }

    @Override
    public String getName() {
        return "achievement";
    }

    @Override
    public String help() {
        return "You got an achievement!\n" +
                "Usage: `" + PREFIX + getName() + " <text>`";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
