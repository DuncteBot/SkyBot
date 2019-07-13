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
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class LinusCommand extends ImageCommandBase {

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passesNoArgs(event)) {
            return;
        }

        final String url = getImageFromCommand(ctx);

        if (url != null) {
            ctx.getBlargbot().getLinus(url).async((image) -> handleBasicImage(event, image));
        }
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Shows a picture of Linus pointing to something on a monitor.\n" +
            "Usage: `" + prefix + "linus [image url]`";
    }

    @NotNull
    @Override
    public String getName() {
        return "linus";
    }
}
