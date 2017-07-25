package me.duncte123.skybot.utils;

import java.util.ArrayList;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;


public class CommandParser {

	public CommandContainer parse(String rw, MessageReceivedEvent e){
		ArrayList<String> split = new ArrayList<String>();
		String raw = rw;
		String beheaded = raw.replaceFirst(Config.prefix, "");
		String[] SplitBeheadded = beheaded.split(" ");
		for(String s : SplitBeheadded){split.add(s);}
		String invoke = split.get(0).toLowerCase();
		String[] args = new String[split.size() - 1];
		split.subList(1, split.size()).toArray(args);
		
		return new CommandContainer(raw, beheaded, SplitBeheadded, invoke, args, e);
	}
	
	 public class CommandContainer {
		 public final String raw;
		 public final String beheaded;
		 public final String[] SplitBeheadded;
		 public final String invoke;
		 public final String[] args;
		 public final MessageReceivedEvent event;
		 
		 public CommandContainer(String rw, String beheaded, String[] SplitBeheaded, String invoke, String[] args, MessageReceivedEvent e){
			 this.raw = rw;
			 this.beheaded = beheaded;
			 this.SplitBeheadded = SplitBeheaded;
			 this.invoke = invoke;
			 this.args = args;
			 this.event = e;
		 }
	 }
	
}
