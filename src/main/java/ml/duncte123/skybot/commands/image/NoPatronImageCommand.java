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

import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public abstract class NoPatronImageCommand extends ImageCommandBase {

    @Nullable
    protected String[] splitString(CommandContext ctx) {

        final String[] split = ctx.getArgsDisplay().split("\\|", 2);

        if (split.length < 2) {
            sendMsg(ctx.getEvent(), "Missing arguments, check `" + ctx.getPrefix() + "help " + getName() + "`");
            return null;
        }

        if (split[0].trim().equals("") || split[1].trim().equals("")) {
            sendMsg(ctx.getEvent(), "Missing arguments, check `" + ctx.getPrefix() + "help " + getName() + "`");
            return null;
        }

        return split;
    }

    @NotNull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }
}
