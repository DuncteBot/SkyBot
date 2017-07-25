package me.duncte123.skybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {
	
	public final AudioPlayer player;
	public final TrackScheduler scheduler;
	public final AudioPlayerSenderHandler sendHandler;
	
	public GuildMusicManager(AudioPlayerManager manager){
		player = manager.createPlayer();
		scheduler = new TrackScheduler(player);
		sendHandler = new AudioPlayerSenderHandler(player);
		player.addListener(scheduler);
	}
	
	public AudioPlayerSenderHandler getSendHandler(){
		return sendHandler;
	}

}
