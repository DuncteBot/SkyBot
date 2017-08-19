package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PotatoCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        event.getChannel().sendMessage((new MessageBuilder()).setTTS(true).append("potato").build()).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "POTATO!!!!";
    }
}
