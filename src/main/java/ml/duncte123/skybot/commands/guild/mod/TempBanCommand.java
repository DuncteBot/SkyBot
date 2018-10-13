/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public class TempBanCommand extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (event.getMessage().getMentionedUsers().size() < 1 || args.size() < 2) {
            sendMsg(event, "Usage is `" + PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }

        final User toBan = event.getMessage().getMentionedUsers().get(0);
        if (toBan.equals(event.getAuthor()) &&
            !Objects.requireNonNull(event.getGuild().getMember(event.getAuthor())).canInteract(Objects.requireNonNull(event.getGuild().getMember(toBan)))) {
            sendMsg(event, "You are not permitted to perform this action.");
            return;
        }

        String reason = String.join(" ", args.subList(2, args.size()));
        String[] timeParts = args.get(1).split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+"); //Split the string into ints and letters


        if (!AirUtils.isInt(timeParts[0])) {
            sendMsg(event, "Usage is `" + PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }


        CalculateBanTime calculateBanTime = new CalculateBanTime(event, timeParts).invoke();
        if (calculateBanTime.hasError()) return;

        if (!reason.isEmpty()) {
            String finalUnbanDate = calculateBanTime.getFinalUnbanDate();
            int finalBanTime = calculateBanTime.getFinalBanTime();
            event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                (voidMethod) -> {
                    if (finalBanTime > 0) {
                        ModerationUtils.addBannedUserToDb(ctx.getDatabase(), event.getAuthor().getId(),
                            toBan.getName(), toBan.getDiscriminator(), toBan.getId(), finalUnbanDate, event.getGuild().getId());

                        modLog(event.getAuthor(), toBan, "banned", reason, args.get(1), ctx.getGuild());
                    } else {
                        logger.error("This code should never run");
                        final String newReason = String.join(" ", ctx.getArgs().subList(1, ctx.getArgs().size()));

                        modLog(event.getAuthor(), toBan, "banned", newReason, ctx.getGuild());
                    }
                }
            );
            sendSuccess(event.getMessage());
            return;
        }

        event.getGuild().getController().ban(toBan.getId(), 1, "No reason was provided").queue(
            (v) -> modLog(event.getAuthor(), toBan, "banned", "*No reason was provided.*", ctx.getGuild())
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
            "Usage: `" + PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`";
    }

    private class CalculateBanTime {
        private boolean error;
        private GuildMessageReceivedEvent event;
        private String[] timeParts;
        private String finalUnbanDate;
        private int finalBanTime;

        private CalculateBanTime(GuildMessageReceivedEvent event, String... timeParts) {
            this.event = event;
            this.timeParts = timeParts;
        }

        private boolean hasError() {
            return error;
        }

        private String getFinalUnbanDate() {
            return finalUnbanDate;
        }

        private int getFinalBanTime() {
            return finalBanTime;
        }

        private CalculateBanTime invoke() {
            String unbanDate = "";
            int banTime; // initial value is always 0
            try {
                banTime = Integer.parseInt(timeParts[0]);
            } catch (NumberFormatException e) {
                sendMsg(event, e.getMessage() + " is not a valid number");
                error = true;
                return this;
            } catch (ArrayIndexOutOfBoundsException ignored /* https://youtube.com/DSHelmondGames */) {
                sendMsg(event, "Incorrect time format, use `" + PREFIX + "help " + getName() + "` for more info.");
                error = true;
                return this;
            }
            if (banTime > 0) {
                if (timeParts.length != 2) {
                    sendMsg(event, "Incorrect time format, use `" + PREFIX + "help " + getName() + "` for more info.");
                    error = true;
                    return this;
                }

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dt = new Date(System.currentTimeMillis());

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
