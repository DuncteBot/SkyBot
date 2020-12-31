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

package ml.duncte123.skybot.commands.fun;

import me.duncte123.loadingbar.LoadingBar;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.LocalDate;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class LoadingBarCommand extends Command {

    public LoadingBarCommand() {
        this.category = CommandCategory.FUN;
        this.name = "loadingbar";
        this.aliases = new String[]{
            "progress",
            "progressbar",
            "lb",
        };
        this.help = "Displays a progress bar that shows how much of the year has passed";
        this.botPermissions = new Permission[]{
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_WRITE,
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final double progress = LoadingBar.getPercentage();
        final int year = LocalDate.now().getYear();

        try {
            ctx.getChannel().sendFile(LoadingBar.generateImage(progress), "bar.png")
                .appendFormat("**%s** is **%s**%% complete.", year, progress).queue();
        }
        catch (IOException e) {
            sendMsg(ctx, "Something went wrong with generating the image.");
        }

    }
}
