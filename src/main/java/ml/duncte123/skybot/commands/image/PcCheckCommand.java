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

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class PcCheckCommand extends ImageCommandBase {

    public PcCheckCommand() {
        this.name = "pccheck";
        this.helpFunction = (invoke, prefix) -> "Your pc needs to be checked, but for what?";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <text>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passes(event, ctx.getArgs())) {
            return;
        }

        ctx.getBlargbot().getPcCheck(parseTextArgsForImage(ctx)).async((image) -> handleBasicImage(event, image));
    }
}
