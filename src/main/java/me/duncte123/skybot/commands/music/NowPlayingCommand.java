package me.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class NowPlayingCommand implements Command {

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
        AudioPlayer player = mng.player;

        String msg = "";

        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != null){
            String title = currentTrack.getInfo().title;
            String position = AudioUtils.getTimestamp(currentTrack.getPosition());
            String duration = AudioUtils.getTimestamp(currentTrack.getDuration());

            msg = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                    title, position, duration);
        }else{
            msg = "The player is not currently playing anything!";
        }
        event.getTextChannel().sendMessage(Functions.embedField(au.embedTitle, msg)).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Prints information about the currently playing song (title, current time)";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
