/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CleanupCommand extends Command {

    public final static String help = "performs a cleanup in the channel where the command is run.";

    public CleanupCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
            MessageUtils.sendMsg(event, "You don't have permission to run this command!");
            return;
        }

        int total = 5;
        //Little hack for lambda
        boolean keepPinned = false;

        if (args.size() > 0) {

            if (args.size() == 1 && args.get(0).equalsIgnoreCase("keep-pinned"))
                keepPinned = true;
            else {
                if (args.size() == 2 && args.get(1).equalsIgnoreCase("keep-pinned"))
                    keepPinned = true;
                try {
                    total = Integer.parseInt(args.get(0));
                } catch (NumberFormatException e) {
                    MessageUtils.sendError(event.getMessage());
                    MessageUtils.sendMsg(event, "Error: Amount to clear is not a valid number");
                    return;
                }
                if (total < 2 || total > 100) {
                    MessageUtils.sendMsgAndDeleteAfter(event, 5, TimeUnit.SECONDS, "Error: count must be minimal 2 and maximal 100");
                    return;
                }
            }
        }

        try {
            final boolean keepPinnedFinal = keepPinned;
            event.getChannel().getHistory().retrievePast(total).queue(msgLst -> {
                if (keepPinnedFinal)
                    msgLst = msgLst.stream().filter(message -> !message.isPinned()).collect(Collectors.toList());

                List<Message> failed = msgLst.stream()
                        .filter(message -> message.getCreationTime().isBefore(OffsetDateTime.now().minusWeeks(2))).collect(Collectors.toList());

                msgLst = msgLst.stream()
                        .filter(message -> message.getCreationTime().isAfter(OffsetDateTime.now().minusWeeks(2))).collect(Collectors.toList());

                if (msgLst.size() < 3) {
                    failed.addAll(msgLst);
                    msgLst.clear();
                } else {
                    event.getChannel().deleteMessages(msgLst).queue(null, ignored -> {
                    });
                }

                MessageUtils.sendMsgFormatAndDeleteAfter(event, 10, TimeUnit.SECONDS,
                        "Removed %d messages!\nIt failed for %d messages!", msgLst.size(), failed.size());
                logger.debug(msgLst.size() + " messages removed in channel " +
                        event.getChannel().getName() + " on guild " + event.getGuild().getName());
            }, error -> MessageUtils.sendMsg(event, "ERROR: " + error.getMessage()));
        } catch (Exception e) {
            MessageUtils.sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public String help() {
        return "Performs a cleanup in the channel where the command is run.\n" +
                "Usage: `" + PREFIX + getName() + "[ammount] [keep-pinned]`";
    }

    @Override
    public String getName() {
        return "cleanup";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clear", "purge"};
    }
}
