package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
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

public class BanCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
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

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 2) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + getName() + " <@user> <time><m/d/w/M/Y> [Reason]")).queue();
            return false;
        }

        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
             final User toBan = event.getMessage().getMentionedUsers().get(0);
            if(toBan.equals(event.getAuthor()) &&
                    !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toBan)) ) {
                event.getChannel().sendMessage(AirUtils.embedMessage("You are not permitted to perform this action.")).queue();
                return;
            }
            if(args.length > 1) {
                String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                String[] timeParts = args[1].split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+"); //Split the string into ints and letters

                if(timeParts == null || !AirUtils.isInt(timeParts[0])) {
                    String newReason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                    event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                            (voidMethod) -> {
                                AirUtils.modLog(event.getAuthor(), toBan, "banned", newReason, event.getGuild());
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
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**\n" +
                "Usage: `" + Config.prefix + getName() + " <@user> <time><m/d/w/M/Y> [Reason]`";
    }

    @Override
    public String getName() {
        return "ban";
    }
}
