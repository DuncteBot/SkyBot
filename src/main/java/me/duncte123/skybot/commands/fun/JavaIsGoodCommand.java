package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class JavaIsGoodCommand implements Command {
	
	public final static String help = "because it is.";

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage(Functions.embedImage("https://cdn.discordapp.com/attachments/172645867570987008/212731289428688908/java-anal-sex.jpg")).queue();
	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return help;
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		// TODO Auto-generated method stub

	}

}
