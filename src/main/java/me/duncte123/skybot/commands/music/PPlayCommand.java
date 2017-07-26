package me.duncte123.skybot.commands.music;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.Config;
import me.duncte123.skybot.utils.Functions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class PPlayCommand implements Command {

    public final static String help = "add a playlist to the queue.";

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

        if(!(args.length > 0)){
            event.getChannel().sendMessage(Functions.embedField(SkyBot.au.embedTitle, "To few arguments, use `"+Config.prefix+"pplay <media link>`")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        String toPlay = StringUtils.join(Arrays.asList(args), " ");
        if(!SkyBot.isURL(toPlay)){
            toPlay = "ytsearch: " + toPlay;
        }

        au.loadAndPlay(mng, event.getTextChannel(), toPlay, true);

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub

    }

}
