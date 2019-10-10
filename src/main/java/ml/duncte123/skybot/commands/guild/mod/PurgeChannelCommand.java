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

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.*;

public class PurgeChannelCommand extends ModBaseCommand {

    public PurgeChannelCommand() {
        this.name = "purgechannel";
        this.helpFunction = (prefix, invoke) -> "Purges an entire text channel";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <#channel>`";
        this.userPermissions = new Permission[]{
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY,
        };
        this.botPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final List<TextChannel> channels = FinderUtil.findTextChannels(ctx.getArgsRaw(), ctx.getGuild());

        if (channels.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final TextChannel toPurge = channels.get(0);

        if (toPurge.equals(ctx.getChannel())) {
            sendMsg(ctx, "For security reasons you can not use this command in the channel that you want to purge");
            return;
        }

        toPurge.createCopy()
            .setPosition(toPurge.getPositionRaw())
            .queue(
                (success) -> {
                    toPurge.delete().queue();
                    sendSuccess(ctx.getMessage());
                },
                (error) -> sendError(ctx.getMessage())
            );
    }
}
