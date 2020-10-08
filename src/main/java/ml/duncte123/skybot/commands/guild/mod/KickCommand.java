/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class KickCommand extends ModBaseCommand {

    public KickCommand() {
        this.requiresArgs = true;
        this.name = "kick";
        this.aliases = new String[]{"yeet"};
        this.help = "Kicks a user from the server";
        this.usage = "<@user> [-r reason]";
        this.userPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
        this.botPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this kick"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<Member> mentioned = ctx.getMentionedArg(0);

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + ctx.getArgs().get(0));
            return;
        }

        final Member toKickMember = mentioned.get(0);

        if (!canInteract(ctx.getMember(), toKickMember, "kick", ctx.getChannel())) {
            return;
        }

        try {

            final User toKick = toKickMember.getUser();
            if (toKick.equals(ctx.getAuthor()) || !ctx.getMember().canInteract(toKickMember)) {
                sendMsg(ctx, "You are not permitted to perform this action.");
                return;
            }

            final AuditableRestAction<Void> kickAction = ctx.getGuild()

                .kick(toKickMember)
                .reason("Kicked by " + ctx.getAuthor().getAsTag());

            String reason = null;
            final var flags = ctx.getParsedFlags(this);

            if (flags.containsKey("r")) {
                reason = String.join(" ", flags.get("r"));
                kickAction.reason("Kicked by " + ctx.getAuthor().getAsTag() + ": " + reason);
            }

            final String finalReason = reason;

            kickAction.queue(
                (ignored) -> {
                    ModerationUtils.modLog(ctx.getAuthor(), toKick, "kicked", finalReason, ctx.getGuild());
                    MessageUtils.sendSuccess(ctx.getMessage());
                }
            );
        }
        catch (HierarchyException ignored) {
            sendMsg(ctx, "I can't kick that member because his roles are above or equals to mine.");
        }


    }
}
