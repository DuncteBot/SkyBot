package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class PPlayCommand extends Command {

    public final static String help = "add a playlist to the queue.";

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return false;
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return false;
        }

        if(!(args.length > 0)){
            event.getChannel().sendMessage(AirUtils.embedField(SkyBot.au.embedTitle, "To few arguments, use `"+Config.prefix+"pplay <media link>`")).queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);

        String toPlay = StringUtils.join(Arrays.asList(args), " ");
        if(!AirUtils.isURL(toPlay)){
            toPlay = "ytsearch: " + toPlay;
        }

        au.loadAndPlay(mng, event.getChannel(), toPlay, true);

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

}
