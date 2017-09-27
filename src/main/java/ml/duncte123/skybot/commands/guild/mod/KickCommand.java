package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class KickCommand extends Command {

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

        if (event.getMessage().getMentionedUsers().size() < 1) {
            sendMsg(event, "Usage is " + Settings.prefix + getName() +" <@user> [Reason]");
            return;
        }

        try {

            User toKick = event.getMessage().getMentionedUsers().get(0);
            if(toKick.equals(event.getAuthor()) &&
                    !event.getGuild().getMember(event.getAuthor()).canInteract(event.getGuild().getMember(toKick)) ) {
                sendMsg(event, "You are not permitted to perform this action.");
                return;
            }
                                           //Arrays.copyOfRange(Array, From, to)
            String reason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
            event.getGuild().getController().kick(toKick.getId(), "Kicked by " + event.getAuthor().getName() + "\nReason: " + reason).queue(
                    (noting) -> {
                        AirUtils.modLog(event.getAuthor(), toKick, "kicked", reason, event.getGuild());
                        sendSuccess(event.getMessage());
                    }
            );
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
        return "Kicks a user.";
    }

    @Override
    public String getName() {
        return "kick";
    }
}
