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

public class BanCommand implements Command {

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
            event.getChannel().sendMessage(Functions.embedMessage("Usage is " + Config.prefix + "ban <@user> <days (set to 0 for perm)> [Resson]")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        try {
            User toBan = event.getMessage().getMentionedUsers().get(0);
            String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
            event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                    (noting) -> {
                        if (Integer.parseInt(args[1]) > 0) {
                            Functions.modLog(event.getAuthor(), toBan, "banned", reason, args[1], event);
                        } else {
                            Functions.modLog(event.getAuthor(), toBan, "banned", reason, event);
                        }
                    }
            );
            event.getGuild().getPublicChannel().sendMessage("User " + toBan.getName() + "#"
                    + toBan.getDiscriminator() + " got bent.").queue();
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(Functions.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Bans a user from the guild";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        return;
    }
}
