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

package ml.duncte123.skybot.commands.guild.owner.settings;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class MuteRoleCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (!ctx.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            sendMsg(event, "I need the _Manage Roles_ permission in order for this feature to work.");
            return;
        }

        if (args.isEmpty()) {
            sendMsg(event, "Incorrect usage: `" + Settings.PREFIX + "spamrole <role name/disable>`");
            return;
        }


        if ("disable".equals(args.get(0))) {
            sendMsg(event, "SpamRole feature & SpamFilter has been disabled");
            //Never clean the role's id so activating the filter wont cause issues.
            //GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(0L));
            guild.setSettings(settings.setEnableSpamFilter(false));
            return;
        }

        final List<Role> selfRoles = ctx.getSelfMember().getRoles();

        if (selfRoles.isEmpty()) {
            sendMsg(event, "I need a role above the specified role in order for this feature to work.");
            return;
        }

        final List<Role> foundRoles = FinderUtil.findRoles(ctx.getArgsRaw(), ctx.getGuild())
            .stream().filter(
                (role) -> role.getPosition() < selfRoles.get(0).getPosition()
            ).collect(Collectors.toList());

        if (foundRoles.isEmpty()) {
            sendMsg(event, "I'm sorry but I could not find any roles for your input, " +
                "make sure that the target role is below my role.");
            return;
        }

        final Role foundRole = foundRoles.get(0);
        guild.setSettings(settings.setMuteRoleId(foundRole.getIdLong()));

        sendMsg(event, "SpamRole has been set to " + foundRole.getAsMention());
    }

    @Override
    public String getName() {
        return "muterole";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"spamrole"};
    }

    @Override
    public String help() {
        return "Gives members a role when they spam.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <role>`";
    }
}
