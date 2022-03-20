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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.durationparser.DurationParser;
import me.duncte123.durationparser.ParsedDuration;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.utils.AirUtils;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class RemindmeCommand extends Command {

    public RemindmeCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "remind";
        this.aliases = new String[]{
            "remindme",
        };
        this.help = "Creates a reminder for you, add `--channel` to remind you in the current channel";
        this.usage = "<reminder> -t <number><w/d/h/m/s>";
        this.extraInfo = "Example: `{prefix}remind Clean your room :/ -t 1d5m";
        this.flags = new Flag[]{
            new Flag(
                't',
                "time",
                "Sets the time for the reminder"
            ),
            new Flag(
                'c',
                "channel",
                "When this flag is set you will be reminded in the channel where you executed the command (reminder is in dms by default)"
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
            sendMsg(ctx, "What do you want me to remind you of?\nUsage: " + this.getUsageInstructions(ctx));
            return;
        }

        if (!flags.containsKey("t")) {
            sendMsg(ctx, "It looks like you forgot to add the time, you can do this with the `--time` flag");
            return;
        }

        final Optional<ParsedDuration> optionalDuration = getDuration(flags);

        if (optionalDuration.isEmpty()) {
            sendMsg(ctx, "Incorrect duration format");
            return;
        }

        final ParsedDuration duration = optionalDuration.get();

        if (duration.getMilis() == 0) {
            sendMsg(ctx, "Your specified time is too short or the time syntax is invalid.");
            return;
        }

        if (duration.getSeconds() < 30) {
            sendMsg(ctx, "Minimum duration is 30 seconds");
            return;
        }

        if (duration.getDays() > 1460) { // 4 years in days
            sendMsg(ctx, "Just keep it below 4 years ok ;)");
            return;
        }

        final String reminder = String.join(" ", flags.get("undefined"));

        if (reminder.length() > 255) {
            sendMsg(ctx, "The maximum message length is 255 characters");
            return;
        }

        final OffsetDateTime expireDate = AirUtils.getDatabaseDate(duration);

        createReminder(ctx, expireDate, reminder, flags, duration);
    }

    private Optional<ParsedDuration> getDuration(Map<String, List<String>> flags) {
        try {
            return DurationParser.parse(String.join("", flags.get("t")));
        }
        catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private void createReminder(CommandContext ctx, OffsetDateTime expireDate, String reminder, Map<String, List<String>> flags, ParsedDuration duration) {
        final boolean inChannel = flags.containsKey("c");
        final String where = inChannel ? " here" : "";

        ctx.getDatabaseAdapter().createReminder(
            ctx.getAuthor().getIdLong(),
            reminder,
            expireDate,
            ctx.getChannel().getIdLong(),
            ctx.getMessage().getIdLong(),
            ctx.getJDAGuild().getIdLong(),
            inChannel
        ).thenAccept((pair) -> {
            if (pair.getFirst()) {
                sendMsg(
                    ctx,
                    String.format(
                        "Got it, I'll remind you%s in _%s_ about \"%s\" (Reminder id %d)",
                        where,
                        duration,
                        reminder,
                        pair.getSecond()
                    )
                );
            } else {
                sendMsg(ctx, "Something went wrong while creating the reminder, try again later");
            }
        });
    }
}
