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

package ml.duncte123.skybot.commands.guild.owner.settings;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

abstract class SettingsBase extends Command {

    public SettingsBase() {
        this.displayAliasesInHelp = true;
        // TODO: Delete this and all old settings commands in about a year
        this.category = CommandCategory.UNLISTED;
        this.userPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (isDev(ctx.getAuthor())) {
            execute(ctx);

            return;
        }

        super.executeCommand(ctx);
    }

    protected void showNewHelp(CommandContext ctx, String name, @Nullable String input) {
        if (input == null) {
            sendMsg(ctx, "This command changed, please use `" + ctx.getPrefix() + "settings " + name + '`');
            return;
        }

        sendMsg(ctx, "This command changed, please use `" + ctx.getPrefix() + "settings " + name + " --set " + input + '`');
    }
}
