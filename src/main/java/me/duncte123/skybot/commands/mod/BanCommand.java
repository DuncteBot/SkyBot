package me.duncte123.skybot.commands.mod;

import me.duncte123.skybot.Command;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BanCommand extends Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**";
    }
}
