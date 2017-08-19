package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class SkipCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        boolean playing = true;
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        if(mng.player.getPlayingTrack().equals(null)){
            playing = false;
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The player is not playing.")).queue();
        }

        return playing;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;
        scheduler.nextTrack();

        event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The current track was skipped.")).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "skips the current track";
    }

}
