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

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendError;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class PurgeChannelCommand extends Command {

    public PurgeChannelCommand() {
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

        if (!ctx.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
            sendMsg(event, "I need the Manage Server permission for this command to work, please contact your server administrator about this");
            return;
        }

        if (args.isEmpty()) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <#text-channel>`");
            return;
        }

        final List<TextChannel> channels = FinderUtil.findTextChannels(ctx.getArgsRaw(), ctx.getGuild());

        if (channels.isEmpty()) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <#text-channel>`");
            return;
        }

        final TextChannel toPurge = channels.get(0);

        if (toPurge.equals(ctx.getChannel())) {
            sendMsg(event, "For security reasons you can not use this command in the channel that you want to purge");
            return;
        }

        ctx.getGuild().getController()
            .createCopyOfChannel(toPurge)
            .setPosition(toPurge.getPosition())
            .queue(
                (success) -> toPurge.delete().queue(),
                (error) -> sendError(ctx.getMessage())
            );
    }

    @Override
    public String getName() {
        return "purgechannel";
    }

    @Override
    public String help() {
        return "Purges a text channel.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <#text-channel>`";
    }
}
