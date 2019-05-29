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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

public class ScrollCommand extends ImageCommandBase {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (!passes(ctx.getEvent(), ctx.getArgs(), true)) {
            return;
        }

        ctx.getAlexFlipnote().getScroll(parseTextArgsForImage(ctx))
            .async((image) -> handleBasicImage(ctx.getEvent(), image));
    }

    @Override
    public String getName() {
        return "scroll";
    }

    @Override
    public String help(String prefix) {
        return "The scroll of truth\n" +
            "Usage: `" + prefix + getName() + " <text>`";
    }
}
