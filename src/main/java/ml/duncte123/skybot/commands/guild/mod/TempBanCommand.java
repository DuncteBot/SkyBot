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

import me.duncte123.durationparser.Duration;
import me.duncte123.durationparser.DurationParser;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.*;

public class TempBanCommand extends ModBaseCommand {

    public TempBanCommand() {
        this.name = "tempban";
        this.helpFunction = (invoke, prefix) -> "Temporally bans a user from the server **(THIS WILL DELETE MESSAGES)**";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <@user> <time><w/d/h/m/s> [-r Reason]`";
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
    public void run(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (args.size() < 2) {
            this.sendUsageInstructions(ctx);
            return;
        }

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

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        }

        final Duration duration = getDuration(args.get(1), getName(), ctx.getEvent(), ctx.getPrefix());

        if (duration == null) {
            return;
        }

        final String finalUnbanDate = AirUtils.getDatabaseDateFormat(duration);
        final String fReason = reason;
        final User toBan = toBanMember.getUser();

        ctx.getGuild().ban(toBan.getId(), 1, fReason).queue(
            (__) -> {
                if (duration.getSeconds() > 0) {
                    addBannedUserToDb(ctx.getDatabaseAdapter(), ctx.getAuthor().getIdLong(),
                        toBan.getName(), toBan.getDiscriminator(), toBan.getIdLong(), finalUnbanDate, ctx.getGuild().getIdLong());

                    modLog(ctx.getAuthor(), toBan, "temporally banned", fReason, duration.toString(), ctx.getGuild());
                } else {
                    logger.error("Perm ban code in temp ban ran {}", args);
                    modLog(ctx.getAuthor(), toBan, "banned", fReason, ctx.getGuild());
                }
            }
        );

        sendSuccess(ctx.getMessage());
    }

    @Nullable
    static Duration getDuration(String arg, String name, GuildMessageReceivedEvent event, String prefix) {
        Optional<Duration> optionalDuration;

        try {
            optionalDuration = DurationParser.parse(arg);
        }
        catch (IllegalArgumentException ignored) {
            optionalDuration = Optional.empty();
        }

        if (optionalDuration.isEmpty()) {
            sendMsg(event, "Usage is `" + prefix + name + " <@user> <time><w/d/h/m/s> [Reason]`");

            return null;
        }

        final Duration duration = optionalDuration.get();

        if (duration.getMilis() == 0) {
            sendMsg(event, "Your specified time is too short or the time syntax is invalid.");

            return null;
        }

        if (duration.getMinutes() < 10) {
            sendMsg(event, "Minimum duration is 10 minutes");

            return null;
        }

        return duration;
    }
}
