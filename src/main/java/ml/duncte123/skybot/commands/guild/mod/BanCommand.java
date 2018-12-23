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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class BanCommand extends Command {

    public BanCommand() {
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

        if (event.getMessage().getMentionedUsers().isEmpty() || args.size() < 2) {
            sendMsg(event, "Usage is " + Settings.PREFIX + getName() + " <@user> <Reason>");
            return;
        }

        try {
            final User toBan = event.getMessage().getMentionedUsers().get(0);
            if (toBan.equals(event.getAuthor()) &&
                !event.getMember().canInteract(Objects.requireNonNull(event.getGuild().getMember(toBan)))) {
                sendMsg(event, "You are not permitted to perform this action.");
                return;
            }


            final String reason = String.join(" ", args.subList(1, args.size()));
            event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                (m) -> {
                    modLog(event.getAuthor(), toBan, "banned", reason, ctx.getGuild());
                    sendSuccess(event.getMessage());
                }
            );

        } catch (HierarchyException e) {
            //e.printStackTrace();
            sendMsg(event, "I can't ban that member because his roles are above or equals to mine.");
        }
    }

    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <Reason>`";
    }

    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"dabon"};
    }
}
