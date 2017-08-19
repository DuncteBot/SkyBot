package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class NowPlayingCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
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
        event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, msg)).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Prints information about the currently playing song (title, current time)";
    }

}
