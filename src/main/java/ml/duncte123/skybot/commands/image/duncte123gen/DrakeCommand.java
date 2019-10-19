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

package ml.duncte123.skybot.commands.image.duncte123gen;

import ml.duncte123.skybot.commands.image.NoPatronImageCommand;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class DrakeCommand extends NoPatronImageCommand {

    public DrakeCommand() {
        this.displayAliasesInHelp = true;
        this.name = "drake";
        this.aliases = new String[]{
            "ddrake",
            "dddrake",
        };
        this.helpFunction = (prefix, invoke) -> "Generates the drake meme format";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <top text>|<bottom text>`";
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

        if (split[0].length() > 200 || split[1].length() > 200) {
            sendMsg(ctx, "Please limit your input to 200 characters to either side of the bar");

            return;
        }

        final String invoke = ctx.getInvoke();

        if (invoke.equalsIgnoreCase("ddrake") || invoke.equalsIgnoreCase("dddrake")) {
            final boolean shouldDab = invoke.equalsIgnoreCase("dddrake");
            final byte[] drake = ctx.getApis().getDannyDrake(split[0], split[1], shouldDab);

            handleBasicImage(ctx.getEvent(), drake);

            return;
        }

        final byte[] image = ctx.getApis().getDrakeMeme(split[0], split[1]);
        handleBasicImage(ctx.getEvent(), image);
    }
}
