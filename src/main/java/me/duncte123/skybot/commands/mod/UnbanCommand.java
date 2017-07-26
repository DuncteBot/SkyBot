package me.duncte123.skybot.commands.mod;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.util.List;

public class UnbanCommand implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        Permission[] perms = {
                Permission.KICK_MEMBERS,
                Permission.BAN_MEMBERS
        };

        if (!PermissionUtil.checkPermission(event.getMember(), perms)) {
            event.getChannel().sendMessage(Functions.embedMessage("You don't have permission to run this command")).queue();
            return false;
        }

        if (args[0].isEmpty()) {
            event.getChannel().sendMessage(Functions.embedMessage("Usage is " + Config.prefix + "unban <username>")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        try {
            Guild guild = event.getGuild();
            List<User> bannedUsers =  guild.getBans().complete();
            for (User bannedUser : bannedUsers) {
                if (bannedUser.getName().equalsIgnoreCase(args[0])) {
                    guild.getController().unban(bannedUser).reason("Unbanned by " + event.getAuthor().getName()).queue();
                    event.getChannel().sendMessage("User " + bannedUser.getName() + " unbanned.").queue();
                    Functions.modLog(event.getAuthor(), bannedUser, "unbanned", event);
                    return;
                }
            }
            event.getChannel().sendMessage("This user is not banned").queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(Functions.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Unbans a user";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        return;
    }
}
