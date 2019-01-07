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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.commands.image.NoPatronImageCommand;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;

public class DrakeCommand extends NoPatronImageCommand {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        if (!passes(ctx.getEvent(), ctx.getArgs(), false)) {
            return;
        }

        final String[] split = splitString(ctx);

        if (split == null) {
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

    @Override
    public String getName() {
        return "drake";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"ddrake", "dddrake"};
    }

    @Override
    public String help() {
        return "Did you type your search wrong?\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <Top text>|<Bottom text>`";
    }
}
