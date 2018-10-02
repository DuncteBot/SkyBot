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

import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class KickCommand extends Command {

    public KickCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
            MessageUtils.sendMsg(event, "You need the kick members permission to use this command, please contact your server administrator about this.");
            return;
        }

        if (event.getMessage().getMentionedUsers().size() < 1) {
            MessageUtils.sendMsg(event, "Usage is " + PREFIX + getName() + " <@user> [Reason]");
            return;
        }

        try {

            User toKick = event.getMessage().getMentionedUsers().get(0);
            if (toKick.equals(event.getAuthor()) ||
                !event.getMember().canInteract(event.getGuild().getMember(toKick))) {
                MessageUtils.sendMsg(event, "You are not permitted to perform this action.");
                return;
            }
            //Arrays.copyOfRange(Array, From, to)
            String reason = StringUtils.join(args.subList(1, args.size()), " ");
            event.getGuild().getController().kick(toKick.getId(), "Kicked by " + event.getAuthor().getName() + "\nReason: " + reason).queue(
                (noting) -> {
                    ModerationUtils.modLog(event.getAuthor(), toKick, "kicked", reason, ctx.getGuild());
                    MessageUtils.sendSuccess(event.getMessage());
                }
            );
        } catch (HierarchyException ignored) { // if we don't do anything with it and just catch it we should name it "ignored"
            //e.printStackTrace();
            MessageUtils.sendMsg(event, "I can't kick that member because his roles are above or equals to mine.");
        }


    }

    @Override
    public String help() {
        return "Kicks a user.";
    }

    @Override
    public String getName() {
        return "kick";
    }
}
