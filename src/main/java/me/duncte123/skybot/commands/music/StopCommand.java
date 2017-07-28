package me.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StopCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return false;
        }

        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        if(mng.player.getPlayingTrack().equals(null)){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The player is not playing.")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;

        scheduler.queue.clear();
        player.stopTrack();
        player.setPaused(false);
        event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "Playback has been completely stopped and the queue has been cleared")).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "stops the music player.";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;
    }

}
