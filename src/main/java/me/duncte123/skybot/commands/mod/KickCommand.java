package me.duncte123.skybot.commands.mod;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class KickCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        Permission[] perms = {
                Permission.MESSAGE_MANAGE,
                Permission.ADMINISTRATOR
        };

        if (!PermissionUtil.checkPermission(event.getMember(), perms) || event.getAuthor().getId().equals("191231307290771456")) {
            event.getChannel().sendMessage(Functions.embedMessage("You don't have permission to run this command")).queue();
            return false;
        }

        if (event.getMessage().getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(Functions.embedMessage("Usage is " + Config.prefix + "kick <@user> [Resson]")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        try {

            User toKick = event.getMessage().getMentionedUsers().get(0);
                                           //Arrays.copyOfRange(Array, From, to)
            String reason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
            event.getGuild().getController().kick(toKick.getId(), reason).queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(Functions.embedMessage("ERROR: " + e.getMessage())).queue();
        }


    }

    @Override
    public String help() {
        return "Kicks a user.";
    }

    @Override
    public void executed(boolean save, MessageReceivedEvent event) {
        return;
    }
}
