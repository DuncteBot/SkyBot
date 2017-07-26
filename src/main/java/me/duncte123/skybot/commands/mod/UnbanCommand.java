package me.duncte123.skybot.commands.mod;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

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

        if (event.getMessage().getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(Functions.embedMessage("Usage is " + Config.prefix + "unban <@user>")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        try {
            event.getGuild().getController().unban(event.getMessage().getMentionedUsers().get(0));
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
