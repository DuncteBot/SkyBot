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

import me.duncte123.durationparser.DurationParser;
import me.duncte123.durationparser.ParsedDuration;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.*;

public class TempBanCommand extends ModBaseCommand {

    public TempBanCommand() {
        this.requiresArgs = true;
        this.requiredArgCount = 2;
        this.name = "tempban";
        this.help = "Temporally bans a user from the server **(THIS WILL DELETE MESSAGES)**";
        this.usage = "<@user> <time><w/d/h/m/s> [-r Reason]";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
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
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedArg(0);

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + args.get(0));
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (toBanMember.equals(ctx.getMember())) {
            sendMsg(ctx, "You cannot ban yourself");
            return;
        }

        if (!canInteract(ctx.getMember(), toBanMember, "ban", ctx.getChannel())) {
            return;
        }

        final ParsedDuration duration = getDuration(args.get(1), getName(), ctx, ctx.getPrefix());

        if (duration == null) {
            return;
        }

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        } else if (args.size() > 2) {
            final var example = "\nExample: `%stempban %s %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), args.get(1), String.join(" ", args.subList(2, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        final String finalUnbanDate = AirUtils.getDatabaseDateFormat(duration);
        final String fReason = reason;
        final User toBan = toBanMember.getUser();

        ctx.getGuild().ban(toBan.getId(), 1, String.format("%#s: %s", ctx.getAuthor(), fReason)).queue(
            (ignored) -> {
                if (duration.getSeconds() > 0) {
                    ctx.getDatabaseAdapter().createBan(
                        ctx.getAuthor().getIdLong(),
                        toBan.getName(),
                        toBan.getDiscriminator(),
                        toBan.getIdLong(),
                        finalUnbanDate,
                        ctx.getGuild().getIdLong()
                    );

                    modLog(ctx.getAuthor(), toBan, "banned", fReason, duration.toString(), ctx.getGuild());
                } else {
                    LOGGER.error("Perm ban code in temp ban ran {}", args);
                    modLog(ctx.getAuthor(), toBan, "banned", fReason, null, ctx.getGuild());
                }
            }
        );

        sendSuccess(ctx.getMessage());
    }

    @Nullable
    public static ParsedDuration getDuration(String arg, String name, CommandContext ctx, String prefix) {
        Optional<ParsedDuration> optionalDuration;

        try {
            optionalDuration = DurationParser.parse(arg);
        }
        catch (IllegalArgumentException ignored) {
            optionalDuration = Optional.empty();
        }

        if (optionalDuration.isEmpty()) {
            sendMsg(ctx, "Usage is `" + prefix + name + " <@user> <time><w/d/h/m/s> [Reason]`");

            return null;
        }

        final ParsedDuration duration = optionalDuration.get();

        if (duration.getMilis() == 0) {
            sendMsg(ctx, "Your specified time is too short or the time syntax is invalid.");

            return null;
        }

        if (duration.getSeconds() < 30) {
            sendMsg(ctx, "Minimum duration is 30 seconds");

            return null;
        }

        return duration;
    }
}
