package me.duncte123.skybot.commands.music;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.audio.TrackScheduler;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ShuffleCommand extends Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
    
    AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;

        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return false;
        }
    
        if (!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())) {
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        if(scheduler.queue.isEmpty()){
            event.getChannel().sendMessage(AirUtils.embedField(au.embedTitle, "There are no songs to shuffle")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;
        scheduler.shuffle();

        event.getTextChannel().sendMessage(AirUtils.embedField(au.embedTitle, "The queue has been shuffled!")).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Makes the player repeat the currently playing song";
    }

}
