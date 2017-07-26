package me.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class PlayCommand implements Command {

    public final static String help = "make the bot play song.";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(Functions.embedField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return false;
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(Functions.embedField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
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

        EmbedBuilder eb = Functions.defaultEmbed();

        if(args.length == 0){
            if(player.isPaused()){
                player.setPaused(false);
                eb.addField(SkyBot.au.embedTitle, "Playback has been resumed.", false);
            }else if(player.getPlayingTrack() != null){
                eb.addField(SkyBot.au.embedTitle, "Player is already playing!", false);
            }else if(scheduler.queue.isEmpty()){
                eb.addField(SkyBot.au.embedTitle, "The current audio queue is empty! Add something to the queue first!", false);
            }
            event.getTextChannel().sendMessage(eb.build()).queue();
        }else{
            String toPlay = StringUtils.join(args, " ");
            if(!SkyBot.isURL(toPlay) && !toPlay.contains("/root/Desktop/music/")){
                toPlay = "ytsearch: " + toPlay;
            }

            au.loadAndPlay(mng, event.getTextChannel(), toPlay, false);
        }

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;

    }

}
