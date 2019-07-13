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
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Role;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class AutoRoleCommand extends SettingsBase {
    @Override
    public void run(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (rolePermCheck(ctx)) {
            return;
        }

        if (args.isEmpty()) {
            sendMsg(ctx, "Incorrect usage: `" + ctx.getPrefix() + "autorole <role name/disable>`");
            return;
        }

        if ("disable".equals(args.get(0))) {
            sendMsg(ctx, "AutoRole feature has been disabled");
            guild.setSettings(settings.setAutoroleRole(0L));
            return;
        }

        final Role foundRole = getFoundRoleOrNull(ctx);

        if (foundRole == null) {
            return;
        }

        guild.setSettings(settings.setAutoroleRole(foundRole.getIdLong()));

        sendMsg(ctx, "AutoRole has been set to " + foundRole.getAsMention());
    }

    @NotNull
    @Override
    public String getName() {
        return "autorole";
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "Gives members a role when they join\n" +
            "Usage: `" + prefix + getName() + " <role>`";
    }

    @Nullable
    static Role getFoundRoleOrNull(CommandContext ctx) {
        final Role foundRole = FinderUtil.findRoles(ctx.getArgsRaw(), ctx.getGuild())
            .stream()
            .filter((role) -> ctx.getSelfMember().canInteract(role))
            .findFirst()
            .orElse(null);

        if (foundRole == null) {
            sendMsg(ctx, "I'm sorry but I could not find any roles for your input, " +
                "make sure that the target role is below my role.");
            return null;
        }

        return foundRole;
    }
}
