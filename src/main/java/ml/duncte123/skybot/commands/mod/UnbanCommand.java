package ml.duncte123.skybot.commands.mod;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

public class UnbanCommand extends Command {
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

        if (args[0].isEmpty()) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "unban <username>")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            List<User> bannedUsers =  guild.getBans().complete();
            for (User bannedUser : bannedUsers) {
                if (bannedUser.getName().equalsIgnoreCase(args[0])) {
                    guild.getController().unban(bannedUser).reason("Unbanned by " + event.getAuthor().getName()).queue();
                    event.getChannel().sendMessage("User " + bannedUser.getName() + " unbanned.").queue();
                    AirUtils.modLog(event.getAuthor(), bannedUser, "unbanned", event);
                    return;
                }
            }
            event.getChannel().sendMessage("This user is not banned").queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Unbans a user";
    }

}
