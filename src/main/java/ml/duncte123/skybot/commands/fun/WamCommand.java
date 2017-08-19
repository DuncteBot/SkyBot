package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class WamCommand extends Command {

    public final static String help = "you need more WAM!.";

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage(AirUtils.embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)") ).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
