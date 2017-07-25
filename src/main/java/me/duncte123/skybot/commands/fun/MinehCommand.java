package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class MinehCommand implements Command {
	
	public final static String help = "HERE COMES MINEH!";

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		event.getTextChannel().sendTyping();
		event.getTextChannel().sendMessage(new MessageBuilder().setTTS(true).append("Insert creepy music here").build()).queueAfter(4, TimeUnit.SECONDS);
		event.getTextChannel().sendTyping();
		event.getTextChannel().sendMessage(Functions.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg")).queueAfter(4, TimeUnit.SECONDS);
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return help;
	}

}
