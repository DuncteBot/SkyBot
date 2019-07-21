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

public class DidYouMeanCommand extends NoPatronImageCommand {

    public DidYouMeanCommand() {
        this.name = "didyoumean";
        this.helpFunction = (invoke, prefix) -> "Did you type your search wrong?";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <top text>|<bottom text>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        if (!passes(ctx.getEvent(), ctx.getArgs(), false)) {
            return;
        }

        final String[] split = splitString(ctx);

        if (split == null) {
            return;
        }

        ctx.getAlexFlipnote().getDidYouMean(split[0], split[1])
            .async((image) -> handleBasicImage(ctx.getEvent(), image));

    }
}
