package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CookieCommand extends Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage("<:blobnomcookie_secret:317636549342789632>").queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "blobnomcookie";
    }

}
