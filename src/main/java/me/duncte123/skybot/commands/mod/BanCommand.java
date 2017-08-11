package me.duncte123.skybot.commands.mod;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import me.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class BanCommand extends Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        Permission[] perms = {
                Permission.KICK_MEMBERS,
                Permission.BAN_MEMBERS
        };

        if (!PermissionUtil.checkPermission(event.getMember(), perms)) {
            event.getChannel().sendMessage(AirUtils.embedMessage("You don't have permission to run this command")).queue();
            return false;
        }

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 2) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "ban <@user> [Resson]")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        final User toBan = event.getMessage().getMentionedUsers().get(0);
        if(toBan.equals(event.getAuthor()) &&
                !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toBan)) ) {
            event.getChannel().sendMessage(AirUtils.embedMessage("You are not permitted to perform this action.")).queue();
            return;
        }


        String reason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");

        event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                (smt) -> AirUtils.modLog(event.getAuthor(), toBan, "banned", reason, event)
        );

    }

    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**";
    }
}
