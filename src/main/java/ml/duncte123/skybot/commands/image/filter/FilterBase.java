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

package ml.duncte123.skybot.commands.image.filter;

import ml.duncte123.skybot.commands.image.NoPatronImageCommand;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public abstract class FilterBase extends NoPatronImageCommand {

    FilterBase() {
        this.name = getClass().getSimpleName().replaceFirst("Command", "").toLowerCase();
        this.help = "Overlays a " + this.name + " filter over the provided image";
        this.usage = "[image url]";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passesNoArgs(event, false)) {
            return;
        }

        final String url = getImageFromCommand(ctx);

        if (url == null) {
            sendMsg(ctx, "Could not find image, please mention a user, upload an image, or put an image url after the command");
        } else {
            final byte[] image = ctx.getApis().getFilter(this.getFilterName(), url);
            handleBasicImage(event, image);
        }
    }

    String getFilterName() {
        return this.name;
    }
}
