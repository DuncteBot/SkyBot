package ml.duncte123.skybot.commands.mod;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

public class HackbanCommand extends Command {
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

        if (args.length < 1) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Usage is " + Config.prefix + "hackban <userId>")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            event.getGuild().getController().ban(args[0], 0).queue( (v) -> {
                event.getChannel().sendMessage(AirUtils.embedMessage("User has been banned!")).queue();
            } );
        }
        catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(AirUtils.embedMessage("ERROR: " + e.getMessage())).queue();
        }
    }

    @Override
    public String help() {
        return "Ban a user before he/she can join your guild.\nUsage: " + Config.prefix + "hackban <userId>";
    }
}
