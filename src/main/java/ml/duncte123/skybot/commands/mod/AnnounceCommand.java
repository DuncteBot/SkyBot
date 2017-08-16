package ml.duncte123.skybot.commands.mod;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AnnounceCommand extends Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        Permission[] perms = {
                Permission.ADMINISTRATOR
        };

        if(!PermissionUtil.checkPermission(event.getMember(), perms)) {
            event.getChannel().sendMessage(AirUtils.embedMessage("I'm sorry but you don't have permission to run this command.")).queue();
            return false;
        }

        if(event.getMessage().getMentionedChannels().size() < 1) {
            event.getChannel().sendMessage(AirUtils.embedMessage("Correct usage is `" + Config.prefix + "announce [#Channel] [Message]`")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        try{

            TextChannel chann = event.getMessage().getMentionedChannels().get(0);
            String msg = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");

            chann.sendMessage(AirUtils.embedMessage(msg)).queue();

        }
        catch (Exception e) {
            event.getChannel().sendMessage(AirUtils.embedMessage("WHOOPS: " + e.getMessage())).queue();
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "Announces a message.";
    }
}
