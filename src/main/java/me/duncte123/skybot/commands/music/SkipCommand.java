package me.duncte123.skybot.commands.music;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SkipCommand implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        boolean playing = true;
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        if(mng.player.getPlayingTrack().equals(null)){
            playing = false;
            event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The player is not playing.")).queue();
        }

        return playing;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;
        scheduler.nextTrack();

        event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The current track was skipped.")).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "skips the current track";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
