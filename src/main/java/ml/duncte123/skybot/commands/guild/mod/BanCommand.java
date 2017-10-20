/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BanCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param invoke
     * @param args The command agruments
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        Permission[] perms = {
                Permission.KICK_MEMBERS,
                Permission.BAN_MEMBERS
        };

        if (!event.getMember().hasPermission(perms)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 2) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() + " <@user> <time><m/d/w/M/Y> [Reason]");
            return;
        }

        try {
             final User toBan = event.getMessage().getMentionedUsers().get(0);
            if(toBan.equals(event.getAuthor()) &&
                    !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toBan)) ) {
                sendMsg(event, "You are not permitted to perform this action.");
                return;
            }
            if(args.length > 1) {
                String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                String[] timeParts = args[1].split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+"); //Split the string into ints and letters

                if(!AirUtils.isInt(timeParts[0])) {
                    String newReason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                    event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                            (voidMethod) -> {
                                AirUtils.modLog(event.getAuthor(), toBan, "banned", newReason, event.getGuild());
                                sendSuccess(event.getMessage());
                                AirUtils.getPublicChannel(event.getGuild()).sendMessage("User " + String.format("%#s", toBan) + " got bent.").queue();
                            }
                    );
                    return;
                }

                String unbanDate = "";
                if (Integer.parseInt(timeParts[0]) > 0) {
                    //TODO make ban timed

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dt = new Date(System.currentTimeMillis());

                    switch (timeParts[1]) {
                        case "m":
                            if(Integer.parseInt(timeParts[0]) < 10 ) {
                                sendMsg(event, "The minimum time for minutes is 10.");
                                return;
                            }
                            dt = DateUtils.addMinutes(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "h":
                            dt = DateUtils.addHours(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "d":
                            dt = DateUtils.addDays(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "w":
                            dt = DateUtils.addWeeks(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "M":
                            dt = DateUtils.addMonths(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "Y":
                            dt = DateUtils.addYears(dt, Integer.parseInt(timeParts[0]));
                            break;

                        default:
                            event.getChannel().sendMessage(timeParts[1]+" is not defined, please choose from m, d, h, w, M or Y").queue();
                            return;
                    }
                    unbanDate = df.format(dt);
                }

                final String finalUnbanDate = (unbanDate == null || unbanDate.isEmpty() ? "" : unbanDate);
                event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                        (voidMethod) -> {
                            if (Integer.parseInt(timeParts[0]) > 0) {
                                AirUtils.addBannedUserToDb(event.getAuthor().getId(), toBan.getName(), toBan.getDiscriminator(), toBan.getId(), finalUnbanDate, event.getGuild().getId());

                                AirUtils.modLog(event.getAuthor(), toBan, "banned", reason, args[1], event.getGuild());
                            } else {
                                final String newReason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                                AirUtils.modLog(event.getAuthor(), toBan, "banned", newReason, event.getGuild());
                            }
                        }
                );
                AirUtils.getPublicChannel(event.getGuild()).sendMessage("User " + String.format("%#s", toBan) + " got bent.").queue();
            } else {
                event.getGuild().getController().ban(toBan.getId(), 1, "No reason was provided").queue(
                        (voidm) -> AirUtils.modLog(event.getAuthor(), toBan, "banned", "*No reason was provided.*", event.getGuild())
                );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**\n" +
                "Usage: `" + Settings.prefix + getName() + " <@user> <time><m/d/w/M/Y> [Reason]`";
    }

    @Override
    public String getName() {
        return "ban";
    }
}
