/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.objects.command.Flag;

import javax.annotation.Nonnull;

public class ShitCommand extends NoPatronImageCommand {

    public ShitCommand() {
        this.name = "shit";
        this.aliases = new String[]{
            "pluralshit",
        };
        this.help = "Exclaim that something is shit";
        this.usage = "<text> [--plural]";
        this.flags = new Flag[]{
            new Flag(
                'p',
                "plural",
                "Make this exclamation plural (is vs are)"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!passes(ctx, false)) {
            return;
        }

        final String text = parseTextArgsForImage(ctx);

        if (ctx.getParsedFlags(this).containsKey("p")) {
            ctx.getBlargbot().getShit(text, true).async((image) -> handleBasicImage(ctx, image));
            return;
        }

        ctx.getBlargbot().getShit(text).async((image) -> handleBasicImage(ctx, image));
    }
}
