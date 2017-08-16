package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class WamCommand extends Command {

    public final static String help = "you need more WAM!.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage(AirUtils.embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)") ).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}
