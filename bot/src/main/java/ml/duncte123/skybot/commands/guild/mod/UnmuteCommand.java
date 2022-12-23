/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.guild.mod;

import com.dunctebot.models.settings.GuildSetting;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

public class UnmuteCommand extends ModBaseCommand {

    public UnmuteCommand() {
        this.requiresArgs = true;
        this.name = "unmute";
        this.help = "Removes the mute of a user if they are muted";
        this.usage = "<@user> [-r reason]";
        this.botPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this umnute"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<Member> mentioned = ctx.getMentionedArg(0);

        if (mentioned.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final GuildSetting settings = ctx.getGuildSettings();

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(ctx, "No mute/spamrole is set, use `" + ctx.getPrefix() + "muterole <Role>` to set it");
            return;
        }
        final Role role = ctx.getGuild().getRoleById(settings.getMuteRoleId());

        if (role == null) {
            sendMsg(ctx, "The current mute role does not exist on this server, please contact your server administrator about this.");
            return;
        }

        final Member toMute = mentioned.get(0);
        final Member mod = ctx.getMember();

        if (!canInteract(mod, toMute, "unmute", ctx.getChannel().asTextChannel())) {
            return;
        }

        final Member self = ctx.getSelfMember();

        if (!self.canInteract(role)) {
            sendMsg(ctx, "I cannot unmute this member, is the mute role above mine?");
            return;
        }

        String reason = "";
        final var flags = ctx.getParsedFlags(this);
        final List<String> args = ctx.getArgs();

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        } else if (args.size() > 1) {
            final var example = "\nExample: `%sunmute %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), String.join(" ", args.subList(1, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        final String fReason = reason;

        ctx.getGuild().removeRoleFromMember(toMute, role)
            .reason("Unmute by " + String.format("%#s: %s", ctx.getAuthor(), fReason)).queue(success -> {
                ModerationUtils.modLog(ctx.getAuthor(), toMute.getUser(), "unmuted", fReason, null, ctx.getGuild());
                sendSuccess(ctx.getMessage());
            }
        );
    }
}
