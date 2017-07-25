package me.duncte123.skybot.commands.fun;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.Functions;
import me.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DogCommand implements Command {

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
		String base = "https://random.dog/";
		try {
			String jsonString = URLConnectionReader.getText(base+"woof");
			String finalS = base+jsonString;
			
			if(finalS.contains(".mp4")){
                event.getTextChannel().sendMessage(Functions.embedField("A video", "[OMG LOOK AT THIS CUTE VIDEO]("+finalS+")")).queue();
			}else{
                event.getTextChannel().sendMessage(Functions.embedImage(finalS)).queue();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			event.getChannel().sendMessage(Functions.embedMessage("**[OOPS]** Something broke, blame duncte")).queue();
		}

	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return "here is a dog.";
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		// TODO Auto-generated method stub

	}

}
