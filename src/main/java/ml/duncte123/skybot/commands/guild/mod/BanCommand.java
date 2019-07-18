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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class BanCommand extends ModBaseCommand {

    public BanCommand() {
        this.name = "ban";
        this.aliases = new String[] {
            "dabon",
        };
        this.helpFunction = (invoke, prefix) -> "Bans a user from the server **(THIS WILL DELETE MESSAGES)**";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + "<@user> [-r Reason]";
        this.botPermissions = new Permission[] {
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[] {
            new Flag(
                'r',
                "reason",
                "Sets the reason for this ban"
            ),
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedMembers();

        if (mentioned.isEmpty() || args.size() < 2) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (!canInteract(ctx.getMember(), toBanMember, "ban", ctx.getChannel())) {
            return;
        }

        final User toBan = toBanMember.getUser();
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
    }
}
