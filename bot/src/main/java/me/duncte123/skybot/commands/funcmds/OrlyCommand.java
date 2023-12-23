/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.commands.funcmds;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.skybot.objects.command.Command;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class OrlyCommand extends Command {
    public OrlyCommand() {
        this.category = CommandCategory.FUN;
        this.name = "orly";
        this.help = "Shows a random O Rly image from @ThePracticalDev on twitter";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String imageUrl = ctx.getApis().getOrlyImage();

        sendEmbed(ctx, EmbedUtils.embedImage(imageUrl));
    }
}
