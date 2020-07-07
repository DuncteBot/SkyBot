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

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

@Author(nickname = "duncte123", author = "Duncan Sterken")
abstract class SettingsBase extends Command {

    public SettingsBase() {
        this.displayAliasesInHelp = true;
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

    protected boolean shouldDisable(CommandContext ctx) {
        // This call is safe as the flags are cached
        final String query = this.getSetValue(ctx);

        return List.of("disable", "disabled", "off", "remove", "removed", "none").contains(query);
    }

    boolean doesNotPassRolePermCheck(CommandContext ctx) {
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

    protected void showNewHelp(CommandContext ctx, String name, @Nullable String input) {
        if (input == null) {
            sendMsg(ctx, "Command changed, please use `" + ctx.getPrefix() + "settings " + name + '`');
            return;
        }

        sendMsg(ctx, "Command changed, please use `" + ctx.getPrefix() + "settings " + name + " --set " + input + '`');
    }

    @Nullable
    protected Role getFoundRoleOrNull(CommandContext ctx) {
        final List<Role> mentionedRoles = ctx.getMessage().getMentionedRoles();

        final Role foundRole;

        if (mentionedRoles.isEmpty()) {
            final String query = this.getSetValue(ctx);

            foundRole = FinderUtil.findRoles(query, ctx.getGuild())
                .stream()
                .filter((role) -> ctx.getSelfMember().canInteract(role))
                .findFirst()
                .orElse(null);
        } else {
            foundRole = mentionedRoles.get(0);
        }

        if (foundRole == null) {
            sendMsg(ctx, "I'm sorry but I could not find any roles for your input, " +
                "make sure that the target role is below my role.");
            return null;
        }

        return foundRole;
    }

    protected String getSetValue(CommandContext ctx) {
        return String.join(", ", ctx.getParsedFlags(this).get("set"));
    }
}
