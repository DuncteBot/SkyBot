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

public class SoftbanCommand extends Command {

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

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 3) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "softban <@user> <time (set to 0 for perm)> " +
                    "[days? months? years?] [Resson]")).queue();
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
            event.getGuild().getController().ban(toBan.getId(), 1, "Kicked by: " + event.getAuthor().getName()).queue();
            event.getGuild().getController().unban(toBan.getId()).reason("Kicked by: " + event.getAuthor().getName()).queue();
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
        return "Kicks a user from the guild **(THIS WILL DELETE MESSAGES)**";
    }
}
