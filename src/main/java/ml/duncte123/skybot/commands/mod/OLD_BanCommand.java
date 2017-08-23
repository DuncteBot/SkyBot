package ml.duncte123.skybot.commands.mod;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class OLD_BanCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        Permission[] perms = {
                Permission.KICK_MEMBERS,
                Permission.BAN_MEMBERS
        };

        if (!PermissionUtil.checkPermission(event.getMember(), perms)) {
            event.getChannel().sendMessage(AirUtils.embedMessage("You don't have permission to run this command")).queue();
            return false;
        }

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 3) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "ban <@user> <time (set to 0 for perm)> " +
                    "[days? months? years?] [Resson]")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
             final User toBan = event.getMessage().getMentionedUsers().get(0);
            if(toBan.equals(event.getAuthor()) &&
                    !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toBan)) ) {
                event.getChannel().sendMessage(AirUtils.embedMessage("You are not permitted to perform this action.")).queue();
                return;
            }
            if(args.length > 3) {
                String reason = StringUtils.join(Arrays.copyOfRange(args, 3, args.length), " ");
                String time = args[1] + " " + args[2];
                String unbanDate = "";
                if (Integer.parseInt(args[1]) > 0) {
                    //TODO make ban timed

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dt = new Date(System.currentTimeMillis());

                    switch (args[2]) {
                        case "minute":
                            DateUtils.addMinutes(dt, Integer.parseInt(args[1]));
                            break;
                        case "minutes":
                            DateUtils.addMinutes(dt, Integer.parseInt(args[1]));
                            break;
                        case "day":
                            DateUtils.addDays(dt, Integer.parseInt(args[1]));
                            break;
                        case "days":
                            DateUtils.addDays(dt, Integer.parseInt(args[1]));
                            break;
                        case "week":
                            DateUtils.addWeeks(dt, Integer.parseInt(args[1]));
                            break;
                        case "weeks":
                            DateUtils.addWeeks(dt, Integer.parseInt(args[1]));
                            break;
                        case "month":
                            DateUtils.addMonths(dt, Integer.parseInt(args[1]));
                            break;
                        case "months":
                            DateUtils.addMonths(dt, Integer.parseInt(args[1]));
                            break;

                        default:
                            event.getChannel().sendMessage("Please choose from day, minute, week or month").queue();
                            return;
                    }
                    unbanDate = df.format(dt);
                }

                final String finalUnbanDate = (unbanDate == null || unbanDate.isEmpty() ? "" : unbanDate);
                event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                        (noting) -> {
                            if (Integer.parseInt(args[1]) > 0) {
                                AirUtils.addBannedUserToDb(event.getAuthor().getId(), toBan.getName(), toBan.getDiscriminator(), toBan.getId(), finalUnbanDate, event.getGuild().getId());

                                AirUtils.modLog(event.getAuthor(), toBan, "banned", reason, time, event);
                            } else {
                                final String newReason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                                AirUtils.modLog(event.getAuthor(), toBan, "banned", newReason, event);
                            }
                        }
                );
                event.getGuild().getPublicChannel().sendMessage("User " + toBan.getName() + "#"
                        + toBan.getDiscriminator() + " got bent.").queue();
            } else {
                event.getGuild().getController().ban(toBan.getId(), 1, "No reason was provided").queue();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**";
    }
}
