package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class HackbanCommand extends Command {

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

        if (!event.getMember().hasPermission(perms)) {
            sendMsg(event, "You don't have permission to run this command");
            return;
        }

        if (args.length < 1) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() +" <userId>");
            return;
        }

        try {
            event.getGuild().getController().ban(args[0], 0).queue( (v) -> {
                sendMsg(event, "User has been banned!");
            } );
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
        return "Ban a user before he/she can join your guild.\nUsage: " + Settings.prefix + getName() + " <userId>";
    }

    @Override
    public String getName() {
        return "hackban";
    }
}
