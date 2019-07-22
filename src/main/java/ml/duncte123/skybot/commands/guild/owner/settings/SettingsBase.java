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

package ml.duncte123.skybot.commands.guild.owner.settings;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

@Author(nickname = "duncte123", author = "Duncan Sterken")
abstract class SettingsBase extends Command {

    public SettingsBase() {
        this.displayAliasesInHelp = true;
        this.category = CommandCategory.ADMINISTRATION;
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

    boolean rolePermCheck(CommandContext ctx) {
        if (!ctx.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            sendMsg(ctx, "I need the _Manage Roles_ permission in order for this feature to work.");

            return true;
        }

        final List<Role> selfRoles = ctx.getSelfMember().getRoles();

        if (selfRoles.isEmpty()) {
            sendMsg(ctx, "I need a role above the specified role in order for this feature to work.");

            return true;
        }

        return false;
    }

    @Nullable
    protected TextChannel findTextChannel(@Nonnull CommandContext ctx) {
        final List<TextChannel> foundChannels = FinderUtil.findTextChannels(ctx.getArgsRaw(), ctx.getGuild());

        if (foundChannels.isEmpty()) {
            return null;
        }

        return foundChannels.stream()
            .filter(TextChannel::canTalk)
            .findFirst()
            .orElse(null);
    }
}
