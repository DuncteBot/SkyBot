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

package ml.duncte123.skybot.commands.guild.mod;

import me.duncte123.botcommons.StringUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public abstract class ModBaseCommand extends Command {

    Permission[] perms;

    ModBaseCommand() {
        this.category = CommandCategory.MODERATION;
        this.perms = new Permission[]{Permission.KICK_MEMBERS, Permission.BAN_MEMBERS};
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(this.perms)) {
            final String neededPerms = Arrays.stream(this.perms)
                .map(Permission::getName)
                .collect(Collectors.joining("`, `"));
            final String permsFormatted = StringUtils.replaceLast(neededPerms, "`, `", "` and `");

            sendMsg(event, "You need the `" + permsFormatted + "` permissions for this command\n" +
                "Please contact your server administrator if this is incorrect.");
            return;
        }

        if (args.isEmpty()) {
            sendMsg(event, "Missing arguments, check `" + Settings.PREFIX + "help " + getName() + '`');
            return;
        }

        run(ctx);
    }

    public abstract void run(@NotNull CommandContext ctx);
}
