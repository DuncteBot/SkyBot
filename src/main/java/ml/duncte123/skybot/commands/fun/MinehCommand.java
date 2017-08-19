package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class MinehCommand extends Command {

    public final static String help = "HERE COMES MINEH!";

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        event.getChannel().sendTyping();
        event.getChannel().sendMessage(new MessageBuilder().setTTS(true).append("Insert creepy music here").build()).queueAfter(4, TimeUnit.SECONDS);
        event.getChannel().sendTyping();
        event.getChannel().sendMessage(AirUtils.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg")).queueAfter(4, TimeUnit.SECONDS);
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

}
