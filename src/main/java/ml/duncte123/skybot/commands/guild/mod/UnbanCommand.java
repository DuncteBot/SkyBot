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

import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class UnbanCommand extends Command {

    public UnbanCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();
        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            MessageUtils.sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (args.size() < 1) {
            MessageUtils.sendMsg(event, "Usage is " + Settings.PREFIX + getName() + " <username>");
            return;
        }

        try {
            event.getGuild().getBanList().queue(list -> {
                for (Guild.Ban ban : list) {
                    User user = ban.getUser();
                    if (user.getName().equalsIgnoreCase(StringUtils.join(args, " ")) || user.getId().equals(args.get(0))) {
                        event.getGuild().getController().unban(user)
                            .reason("Unbanned by " + event.getAuthor().getName()).queue();
                        MessageUtils.sendMsg(event, "User " + user.getName() + " unbanned.");
                        ModerationUtils.modLog(event.getAuthor(), ban.getUser(), "unbanned", ctx.getGuild());
                        return;
                    }
                }
                MessageUtils.sendMsg(event, "This user is not banned");
            });

        } catch (Exception e) {
            e.printStackTrace();
            MessageUtils.sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public String help() {
        return "Unbans a user";
    }

    @Override
    public String getName() {
        return "unban";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"ban't"};
    }
}
