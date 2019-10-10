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

import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

public class UnmuteCommand extends ModBaseCommand {

    public UnmuteCommand() {
        this.name = "unmute";
        this.helpFunction = (prefix, invoke) -> "Removes the mute of a user if they are muted";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <@user> [-r reason]";
        this.botPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
        };
        this.flags = new Flag[]{
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
        final Member mod = ctx.getMember();
        final Member self = ctx.getSelfMember();
        final GuildSettings settings = ctx.getGuildSettings();

        if (mentioned.isEmpty() || args.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final Member toMute = mentioned.get(0);

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(event, "No mute/spamrole is set, use `" + ctx.getPrefix() + "muterole <Role>` to set it");
            return;
        }
        final Role role = event.getGuild().getRoleById(settings.getMuteRoleId());

        if (role == null) {
            sendMsg(event, "The current mute role does not exist on this server, please contact your server administrator about this.");
            return;
        }

        if (!canInteract(mod, toMute, "unmute", ctx.getChannel())) {
            return;
        }

        if (!self.canInteract(role)) {
            sendMsg(event, "I cannot unmute this member, is the mute role above mine?");
            return;
        }

        String reason = "";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        }

        final String fReason = reason;

        event.getGuild().removeRoleFromMember(toMute, role)
            .reason("Unmute by " + event.getAuthor().getAsTag() + ": " + fReason).queue(success -> {
                ModerationUtils.modLog(event.getAuthor(), toMute.getUser(), "unmuted", fReason, ctx.getGuild());
                sendSuccess(event.getMessage());
            }
        );
    }
}
