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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.durationparser.Duration;
import me.duncte123.durationparser.DurationParser;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class RemindmeCommand extends Command {

    public RemindmeCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "remind";
        this.aliases = new String[]{
            "remindme",
        };
        this.helpFunction = (invoke, prefix) -> "Creates a reminder for you, add `--channel` to remind you in the current channel";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <reminder> -t <number><w/d/h/m/s>`\n" +
            "Example: `" + prefix + "remind 1d5m Clean your room :/`";
        this.flags = new Flag[]{
            new Flag(
                't',
                "time",
                "Sets the time for the reminder"
            ),
            new Flag(
                'c',
                "channel",
                "Tells the reminder to remind you in this channel"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (args.size() < 2) {
            this.sendUsageInstructions(ctx);

            return;
        }

        final var flags = ctx.getParsedFlags(this);

        if (flags.get("undefined").isEmpty()) {
            sendMsg(ctx, "What do you want me to remind you for?");
            return;
        }

        if (!flags.containsKey("t")) {
            sendMsg(ctx, "It looks like you forgot to add the time, you can do this with the `--time` flag");
            return;
        }

        Optional<Duration> optionalDuration;

        try {
            optionalDuration = DurationParser.parse(
                String.join("", flags.get("t"))
            );
        }
        catch (IllegalArgumentException ignored) {
            optionalDuration = Optional.empty();
        }

        if (optionalDuration.isEmpty()) {
            sendMsg(ctx, "Incorrect duration format");

            return;
        }

        final Duration duration = optionalDuration.get();

        if (duration.getMilis() == 0) {
            sendMsg(ctx, "Your specified time is too short or the time syntax is invalid.");

            return;
        }

        if (duration.getMinutes() < 2) {
            sendMsg(ctx, "Minimum duration is 2 minutes");

            return;
        }

        final String reminder = String.join(" ",flags.get("undefined"));
        final Date expireDate = AirUtils.getDatabaseDate(duration);

        if (flags.containsKey("c")) {
            ctx.getDatabaseAdapter().createReminder(
                ctx.getAuthor().getIdLong(),
                reminder,
                expireDate,
                ctx.getChannel().getIdLong(),
                (success) -> {
                    if (success) {
                        sendMsg(ctx, "Got it, I'll remind you here in _" + duration + "_ about \"" + reminder + "\"");
                    } else {
                        sendMsg(ctx, "Something went wrong while creating the reminder, try again later");
                    }

                    return null;
                });

            return;
        }

        ctx.getDatabaseAdapter().createReminder(ctx.getAuthor().getIdLong(), reminder, expireDate, (success) -> {
            if (success) {
                sendMsg(ctx, "Got it, I'll remind you in _" + duration + "_ about \"" + reminder + "\"");
            } else {
                sendMsg(ctx, "Something went wrong while creating the reminder, try again later");
            }

            return null;
        });
    }
}
