/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

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
                            DateUtils.addMinutes(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "d":
                            DateUtils.addDays(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "w":
                            DateUtils.addWeeks(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "M":
                            DateUtils.addMonths(dt, Integer.parseInt(timeParts[0]));
                            break;
                        case "Y":
                            DateUtils.addYears(dt, Integer.parseInt(timeParts[0]));
                            break;

                        default:
                            event.getChannel().sendMessage(timeParts[1]+" is not defined, please choose from m, d, w, M or Y").queue();
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
