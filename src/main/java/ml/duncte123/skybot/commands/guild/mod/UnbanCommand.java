package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

public class UnbanCommand extends Command {

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

        if (!PermissionUtil.checkPermission(event.getMember(), perms)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (args[0].isEmpty()) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() +" <username>");
            return;
        }

        try {
            Guild guild = event.getGuild();
            List<User> bannedUsers =  guild.getBans().complete();
            for (User bannedUser : bannedUsers) {
                if (bannedUser.getName().equalsIgnoreCase(args[0])) {
                    guild.getController().unban(bannedUser).reason("Unbanned by " + event.getAuthor().getName()).queue();
                    event.getChannel().sendMessage("User " + bannedUser.getName() + " unbanned.").queue();
                    AirUtils.modLog(event.getAuthor(), bannedUser, "unbanned", event.getGuild());
                    return;
                }
            }
            event.getChannel().sendMessage("This user is not banned").queue();
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
        return "Unbans a user";
    }

    @Override
    public String getName() {
        return "unban";
    }
}
