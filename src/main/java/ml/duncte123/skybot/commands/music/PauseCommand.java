package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PauseCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {


        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;

        if (player.getPlayingTrack() == null){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "Cannot pause or resume player because no track is loaded for playing.")).queue();
            return;
        }

        player.setPaused(!player.isPaused());
        if (player.isPaused()){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The player has been paused.")).queue();
        }else{
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The player has resumed playing.")).queue();
        }
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "pauses the current song";
    }
}
