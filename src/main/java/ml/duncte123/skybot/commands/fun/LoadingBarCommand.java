/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Calendar;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class LoadingBarCommand extends Command {

    public LoadingBarCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (!ctx.getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_WRITE)) {
            sendMsg(ctx.getEvent(), "I need the `Attach Files` permission for this command to work");
            return;
        }

        final double progress = LoadingBar.getPercentage();
        final int year = Calendar.getInstance().getWeekYear();

        try {
            ctx.getChannel().sendFile(LoadingBar.generateImage(progress), "bar.png")
                .appendFormat("**%s** is **%s**%% complete.", year, progress).queue();
        } catch (IOException e) {
            sendMsg(ctx.getEvent(), "Something went wrong with generating the image.");
        }

    }

    @Override
    public String getName() {
        return "loadingbar";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"progress", "progressbar"};
    }

    @Override
    public String help() {
        return "Displays a progress bar that shows how much of the year has passed";
    }
}
