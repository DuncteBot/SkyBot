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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.*;

public class TempBanCommand extends ModBaseCommand {

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedMembers();

        if (mentioned.isEmpty() || args.size() < 2) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><w/d/h/m/s> [Reason]`");
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (toBanMember.equals(event.getMember())) {
            sendMsg(event, "You cannot ban yourself");
            return;
        }

        if (!canInteract(ctx.getMember(), toBanMember, "ban", ctx.getChannel())) {
            return;
        }

        final String reason = String.join(" ", args.subList(2, args.size()));
        final Optional<Duration> optionalDuration = DurationParser.parse(args.get(1));

        if (!optionalDuration.isPresent()) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><w/d/h/m/s> [Reason]`");
            return;
        }

        final Duration duration = optionalDuration.get();

        if (duration.getMilis() == 0) {
            sendMsg(event, "Your specified time is too short or the time syntax is invalid.");
            return;
        }

        final String finalUnbanDate = getBanDateFormat(duration);
        final String fReason = reason.isEmpty() ? "No reason was provided" : reason;
        final User toBan = toBanMember.getUser();

        event.getGuild().getController().ban(toBan.getId(), 1, fReason).queue(
            (__) -> {
                if (duration.getSeconds() > 0) {
                    addBannedUserToDb(ctx.getDatabaseAdapter(), event.getAuthor().getIdLong(),
                        toBan.getName(), toBan.getDiscriminator(), toBan.getIdLong(), finalUnbanDate, event.getGuild().getIdLong());

                    modLog(event.getAuthor(), toBan, "temporally banned", fReason, duration.toString(), ctx.getGuild());
                } else {
                    logger.error("Perm ban code in temp ban ran {}", args);
                    modLog(event.getAuthor(), toBan, "banned", fReason, ctx.getGuild());
                }
            }
        );

        sendSuccess(event.getMessage());
    }

    @Override
    public String getName() {
        return "tempban";
    }

    @Override
    public String help() {
        return "Temporally bans a user from the guild **(THIS WILL DELETE MESSAGES)**\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <time><w/d/h/m/s> [Reason]`";
    }


    static String getBanDateFormat(Duration duration) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final Calendar c = Calendar.getInstance();

        c.setTimeInMillis(System.currentTimeMillis() + duration.getMilis());

        return df.format(c.getTime());
    }
}
