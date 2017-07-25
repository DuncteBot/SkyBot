package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DankWeedCommand implements Command {
	
	public final static String help = "ignore this one.";

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
		event.getTextChannel().sendMessage(Functions.embedImage("https://cdn.discordapp.com/attachments/203624252295872513/214335018418307073/and-i-dont-care.jpg")).queue();
	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return help;
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return;
	}

}
