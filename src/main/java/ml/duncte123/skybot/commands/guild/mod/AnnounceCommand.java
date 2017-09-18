package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AnnounceCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        Permission[] perms = {
                Permission.ADMINISTRATOR
        };

        if(!PermissionUtil.checkPermission(event.getMember(), perms)) {
            sendMsg(event, "I'm sorry but you don't have permission to run this command.");
            return;
        }

        if(event.getMessage().getMentionedChannels().size() < 1) {
            sendMsg(event, "Correct usage is `" + Config.prefix + getName() + " [#Channel] [Message]`");
            return;
        }

        try{

            TextChannel chann = event.getMessage().getMentionedChannels().get(0);
            String msg = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");

            sendEmbed(AirUtils.embedMessage(msg), event);

        }
        catch (Exception e) {
            sendMsg(event, "WHOOPS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Announces a message.";
    }

    @Override
    public String getName() {
        return "announce";
    }
}
