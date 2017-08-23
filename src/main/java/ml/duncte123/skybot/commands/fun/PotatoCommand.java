package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PotatoCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        sendMsg(event, (new MessageBuilder()).setTTS(true).append("potato").build());
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "POTATO!!!!";
    }
}
