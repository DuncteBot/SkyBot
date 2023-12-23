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

package me.duncte123.skybot.commands.guild.mod;

import com.dunctebot.models.settings.GuildSetting;
import me.duncte123.durationparser.ParsedDuration;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import me.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.List;

import static com.dunctebot.models.utils.DateUtils.getDatabaseDateFormat;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static me.duncte123.skybot.commands.guild.mod.TempBanCommand.getDuration;
import static me.duncte123.skybot.utils.ModerationUtils.canInteract;

public class TempMuteCommand extends ModBaseCommand {

    public TempMuteCommand() {
        this.requiresArgs = true;
        this.requiredArgCount = 2;
        this.name = "tempmute";
        this.help = "Temporally mutes a user in the server, this will override any existing tempmutes for the user";
        this.usage = "<@user> <time><w/d/h/m/s> [-r reason]";
        this.userPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
        this.botPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this mute"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<Member> mentioned = ctx.getMentionedArg(0);
        final List<String> args = ctx.getArgs();

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + args.get(0));
            return;
        }

        final GuildSetting settings = ctx.getGuildSettings();

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(ctx, "No mute/spamrole is set, use `" + ctx.getPrefix() + "spamrole <Role>` to set it");
            return;
        }

        final Member mod = ctx.getMember();
        final Member toMute = mentioned.get(0);
        final Guild guild = ctx.getGuild();
        final Role role = guild.getRoleById(settings.getMuteRoleId());
        final Member self = ctx.getSelfMember();

        if (canNotProceed(ctx, mod, toMute, role, self)) {
            return;
        }

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        } else if (args.size() > 2) {
            final var example = "\nExample: `%stempmute %s %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), args.get(1), String.join(" ", args.subList(2, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        final ParsedDuration duration = getDuration(args.get(1), getName(), ctx, ctx.getPrefix());

        if (duration == null) {
            return;
        }

        final String fReason = reason;
        final String finalDate = getDatabaseDateFormat(duration);
        final User mutee = toMute.getUser();
        final User author = ctx.getAuthor();

        ctx.getDatabase().createMute(
            author.getIdLong(),
            mutee.getIdLong(),
            mutee.getAsTag(),
            finalDate,
            guild.getIdLong()
        ).thenAccept((mute) -> {
            if (mute != null) {
                final long modId = mute.getModId();
                final User oldMuteMod = guild.getJDA().getUserById(modId);
                String modName = "Unknown#0000";

                if (oldMuteMod != null) {
                    modName = oldMuteMod.getAsTag();
                }

                sendMsg(ctx, String.format(
                    "Previously created muted for %#s removed, mute was created by %s",
                    mutee,
                    modName
                ));
            }
        });


        guild.addRoleToMember(toMute, role)
            .reason("Muted by " + String.format("%#s: %s", ctx.getAuthor(), fReason))
            .queue(success -> {
                    ModerationUtils.modLog(author, mutee, "muted", fReason, duration.toString(), ctx.getGuild());
                    sendSuccess(ctx.getMessage());
                }
            );
    }

    /* package */ static boolean canNotProceed(@Nonnull CommandContext ctx, Member mod, Member toMute, Role role, Member self) {
        if (role == null) {
            sendMsg(ctx, "The current mute role does not exist on this server, please contact your server administrator about this.");

            return true;
        }

        if (!canInteract(mod, toMute, "mute", ctx.getChannel())) {
            return true;
        }

        if (!self.canInteract(role)) {
            sendMsg(ctx, "I cannot mute this member, is the mute role above mine?");

            return true;
        }

        return false;
    }
}
