package ml.duncte123.skybot.commands.mod;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.Config;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class KickCommand extends Command {

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

        if (event.getMessage().getMentionedUsers().size() < 1) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "kick <@user> [Resson]")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        try {

            User toKick = event.getMessage().getMentionedUsers().get(0);
            if(toKick.equals(event.getAuthor()) &&
                    !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toKick)) ) {
                event.getChannel().sendMessage(AirUtils.embedMessage("You are not permitted to perform this action.")).queue();
                return;
            }
                                           //Arrays.copyOfRange(Array, From, to)
            String reason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
            event.getGuild().getController().kick(toKick.getId(), reason + "\nKicked by " + event.getAuthor().getName()).queue(
                    (noting) -> AirUtils.modLog(event.getAuthor(), toKick, "kicked", reason, event)
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }


    }

    @Override
    public String help() {
        return "Kicks a user.";
    }
}
