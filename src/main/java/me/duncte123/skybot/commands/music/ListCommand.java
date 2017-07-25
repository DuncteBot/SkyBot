package me.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Queue;

public class ListCommand implements Command {

	@Override
	public boolean called(String[] args, MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void action(String[] args, MessageReceivedEvent event) {
		AudioUtils au = SkyBot.au;

		Guild guild = event.getGuild();
		GuildMusicManager mng = au.getMusicManager(guild);
		TrackScheduler scheduler = mng.scheduler;

		Queue<AudioTrack> queue = scheduler.queue;
		synchronized (queue) {
			if (queue.isEmpty()) {
		        event.getTextChannel().sendMessage(Functions.embedField(au.embedTitle, "The queue is currently empty!")).queue();
			} else {
				int trackCount = 0;
				long queueLength = 0;
				StringBuilder sb = new StringBuilder();
				sb.append("Current Queue: Entries: ").append(queue.size()).append("\n");
				for (AudioTrack track : queue) {
					queueLength += track.getDuration();
					if (trackCount < 10) {
						sb.append("`[").append(AudioUtils.getTimestamp(track.getDuration())).append("]` ");
						sb.append(track.getInfo().title).append("\n");
						trackCount++;
					}
				}
				sb.append("\n").append("Total Queue Time Length: ").append(AudioUtils.getTimestamp(queueLength));
		        event.getTextChannel().sendMessage(Functions.embedField(au.embedTitle, sb.toString())).queue();
			}
		}	
	}

	@Override
	public String help() {
		// TODO Auto-generated method stub
		return "shows the current queue";
	}

	@Override
	public void executed(boolean success, MessageReceivedEvent event) {
		// TODO Auto-generated method stub

	}

}
