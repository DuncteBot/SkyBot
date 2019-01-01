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

package ml.duncte123.skybot.commands.image.filter;

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.commands.image.ImageCommandBase;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public abstract class FilterBase extends ImageCommandBase {

    private final String commandName;

    public FilterBase() {
        this.commandName = getClass().getSimpleName().replaceFirst("Command", "").toLowerCase();
        this.category = CommandCategory.PATRON;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passesNoArgs(event)) {
            return;
        }

        final String url = getImageFromCommand(ctx);

        if (url != null) {
            ctx.getAlexFlipnote().getFilter(getFilterName(), url).async((image) -> handleBasicImage(event, image));
        }
    }

    String getFilterName() {
        return commandName;
    }

    @Override
    public String getName() {
        return commandName;
    }

    @Override
    public String help() {
        return "Overlays a " + getFilterName() + " filter to the provided image.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " [image url]`";
    }
}
