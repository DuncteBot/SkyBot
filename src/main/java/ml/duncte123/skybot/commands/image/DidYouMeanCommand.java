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

import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class DidYouMeanCommand extends ImageCommandBase {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (!passes(ctx.getEvent(), ctx.getArgs(), false)) return;

        String[] split = ctx.getArgsDisplay().split("\\|", 2);

        if (split.length < 2) {
            sendMsg(ctx.getEvent(), "Missing arguments, check `" + PREFIX + "help " + getName() + "`");
            return;
        }

        ctx.getAlexFlipnote().getDidYouMean(split[0], split[1])
            .async((image) -> handleBasicImage(ctx.getEvent(), image));

    }

    @Override
    public String getName() {
        return "didyoumean";
    }

    @Override
    public String help() {
        return "Did you type your search wrong?\n" +
            "Usage: `" + PREFIX + getName() + " <Top text>|<Bottom text>`";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
