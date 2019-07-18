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

import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SoftbanCommand extends ModBaseCommand {

    public SoftbanCommand() {
        this.name = "softban";
        this.helpFunction = (invoke, prefix) -> "Kicks a user from the server **(THIS WILL DELETE MESSAGES)**";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <@user> [-r reason]`";
        this.userPermissions = new Permission[] {
            Permission.KICK_MEMBERS,
        };
        this.botPermissions = new Permission[] {
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[] {
            new Flag(
                'r',
                "reason",
                "Sets the reason for this kick"
            ),
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedMembers();

        if (mentioned.isEmpty() || args.size() < 2) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (!canInteract(ctx.getMember(), toBanMember, "softban", ctx.getChannel())) {
            return;
        }

        try {
            final User toBan = toBanMember.getUser();
            if (toBan.equals(ctx.getAuthor()) &&
                !ctx.getGuild().getMember(ctx.getAuthor()).canInteract(ctx.getGuild().getMember(toBan))) {
                sendMsg(ctx, "You are not permitted to perform this action.");
                return;
            }

            final String reason = String.join(" ", args.subList(1, args.size()));

            ctx.getGuild().getController().ban(toBanMember, 1)
                .reason("Kicked by: " + ctx.getAuthor().getAsTag() + "\nReason: " + reason).queue(
                nothing -> {
                    ModerationUtils.modLog(ctx.getAuthor(), toBan, "kicked", reason, ctx.getGuild());
                    MessageUtils.sendSuccess(ctx.getMessage());
                    ctx.getGuild().getController().unban(toBan.getId())
                        .reason("(softban) Kicked by: " + ctx.getAuthor().getAsTag()).queue();
                }
            );
        }
        catch (HierarchyException ignored) {
            sendMsg(ctx, "I can't ban that member because his roles are above or equals to mine.");
        }
    }
}
