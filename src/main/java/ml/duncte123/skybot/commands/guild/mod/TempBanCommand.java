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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.*;

public class TempBanCommand extends ModBaseCommand {

    @Override
    public void run(@NotNull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedMembers();

        if (mentioned.isEmpty() || args.size() < 2) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (toBanMember.equals(event.getMember())) {
            sendMsg(event, "You are not permitted to perform this action.");
            return;
        }

        if (!canInteract(ctx.getMember(), toBanMember, "ban", ctx.getChannel())) {
            return;
        }

        final String reason = String.join(" ", args.subList(2, args.size()));
        final String[] timeParts = args.get(1).split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+"); //Split the string into ints and letters


        if (!AirUtils.isInt(timeParts[0])) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }


        final CalculateBanTime calculateBanTime = new CalculateBanTime(event, timeParts).invoke();
        if (calculateBanTime.hasError()) {
            return;
        }

        final String finalUnbanDate = calculateBanTime.getFinalUnbanDate();
        final int finalBanTime = calculateBanTime.getFinalBanTime();

        final String fReason = reason.isEmpty() ? "No reason was provided" : reason;
        final User toBan = toBanMember.getUser();

        event.getGuild().getController().ban(toBan.getId(), 1, fReason).queue(
            (voidMethod) -> {
                if (finalBanTime > 0) {
                    addBannedUserToDb(ctx.getDatabaseAdapter(), event.getAuthor().getIdLong(),
                        toBan.getName(), toBan.getDiscriminator(), toBan.getIdLong(), finalUnbanDate, event.getGuild().getIdLong());

                    modLog(event.getAuthor(), toBan, "banned", fReason, args.get(1), ctx.getGuild());
                } else {
                    logger.error("This code should never run");
                    final String newReason = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));

                    modLog(event.getAuthor(), toBan, "banned", newReason, ctx.getGuild());
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
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`";
    }

    class CalculateBanTime {
        private boolean error;
        private GuildMessageReceivedEvent event;
        private String[] timeParts;
        private String finalUnbanDate;
        private int finalBanTime;

        CalculateBanTime(GuildMessageReceivedEvent event, String[] timeParts) {
            this.event = event;
            this.timeParts = timeParts;
        }

        boolean hasError() {
            return error;
        }

        String getFinalUnbanDate() {
            return finalUnbanDate;
        }

        int getFinalBanTime() {
            return finalBanTime;
        }

        CalculateBanTime invoke() {
            String unbanDate = "";
            int banTime; // initial value is always 0
            try {
                banTime = Integer.parseInt(timeParts[0]);
            }
            catch (NumberFormatException e) {
                sendMsg(event, e.getMessage() + " is not a valid number");
                error = true;
                return this;
            }
            catch (ArrayIndexOutOfBoundsException ignored /* https://youtube.com/DSHelmondGames */) {
                sendMsg(event, "Incorrect time format, use `" + Settings.PREFIX + "help " + getName() + "` for more info.");
                error = true;
                return this;
            }
            if (banTime > 0) {
                if (timeParts.length != 2) {
                    sendMsg(event, "Incorrect time format, use `" + Settings.PREFIX + "help " + getName() + "` for more info.");
                    error = true;
                    return this;
                }

                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dt = new Date();

                switch (timeParts[1]) {
                    case "m":
                        if (Integer.parseInt(timeParts[0]) < 10) {
                            sendMsg(event, "The minimum time for minutes is 10.");
                            error = true;
                            return this;
                        }
                        dt = DateUtils.addMinutes(dt, banTime);
                        break;
                    case "h":
                        dt = DateUtils.addHours(dt, banTime);
                        break;
                    case "d":
                        dt = DateUtils.addDays(dt, banTime);
                        break;
                    case "w":
                        dt = DateUtils.addWeeks(dt, banTime);
                        break;
                    case "M":
                        dt = DateUtils.addMonths(dt, banTime);
                        break;
                    case "Y":
                        dt = DateUtils.addYears(dt, banTime);
                        break;

                    default:
                        sendMsg(event, timeParts[1] + " is not defined, please choose from m, d, h, w, M or Y");
                        error = true;
                        return this;
                }
                unbanDate = df.format(dt);
            }

            finalUnbanDate = unbanDate.isEmpty() ? "" : unbanDate;
            finalBanTime = banTime;
            error = false;
            return this;
        }
    }
}
