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

public class JokeoverheadCommand extends NoPatronImageCommand {

    public JokeoverheadCommand() {
        this.name = "jokeoverhead";
        this.aliases = new String[] {
            "woosh",
        };
        this.helpFunction = (invoke, prefix) -> "You just got wooshed";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " [@user/url]`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!passesNoArgs(ctx.getEvent(), false)) {
            return;
        }

        final String url = getImageFromCommand(ctx);
        if (url != null) {
            ctx.getAlexFlipnote().getJokeoverhead(url).async((image) -> handleBasicImage(ctx.getEvent(), image));
        }
    }
}
