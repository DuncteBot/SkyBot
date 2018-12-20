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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;

@Author(nickname = "Sanduhr32", author = "Maurice R S")
public class MuteCommand extends Command {

    public MuteCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (event.getMessage().getMentionedMembers().isEmpty() || args.size() < 2) {
            sendMsg(event, "Usage is " + Settings.PREFIX + getName() + " <@user> <reason>");
            return;
        }

        final GuildSettings settings = ctx.getGuildSettings();

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(event, "No mute/spamrole is set, use `db!spamrole <Role>` to set it");
            return;
        }

        final String reason = StringUtils.join(args.subList(1, args.size()), " ");
        final Member toMute = event.getMessage().getMentionedMembers().get(0);
        final Role role = event.getGuild().getRoleById(settings.getMuteRoleId());

        event.getGuild().getController().addSingleRoleToMember(toMute, role)
            .reason("Muted by" + String.format("%#s", event.getAuthor()) + ": " + reason).queue(success -> {
                ModerationUtils.modLog(event.getAuthor(), toMute.getUser(), "muted", ctx.getGuild());
                sendSuccess(event.getMessage());
            }
        );

    }

    @Override
    public String help() {
        return "Mute a user.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <reason>`";
    }

    @Override
    public String getName() {
        return "mute";
    }
}
