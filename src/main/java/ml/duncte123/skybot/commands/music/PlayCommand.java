package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class PlayCommand extends Command {

    public final static String help = "make the bot play song.";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().sendMessage(AirUtils.embedField(AirUtils.au.embedTitle, "I'm not in a voice channel, use `"+Config.prefix+"join` to make me join a channel")).queue();
            return;
        }

        if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
            event.getChannel().sendMessage(AirUtils.embedField(AirUtils.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands")).queue();
            return;
        }

        AudioUtils au = AirUtils.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);
        AudioPlayer player = mng.player;
        TrackScheduler scheduler = mng.scheduler;

        EmbedBuilder eb = AirUtils.defaultEmbed();

        if(args.length == 0){
            if(player.isPaused()){
                player.setPaused(false);
                eb.addField(au.embedTitle, "Playback has been resumed.", false);
            }else if(player.getPlayingTrack() != null){
                eb.addField(au.embedTitle, "Player is already playing!", false);
            }else if(scheduler.queue.isEmpty()){
                eb.addField(au.embedTitle, "The current audio queue is empty! Add something to the queue first!", false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
        }else{
            String toPlay = StringUtils.join(args, " ");
            if(!AirUtils.isURL(toPlay) && !toPlay.contains("/root/Desktop/music/")){
                toPlay = "ytsearch: " + toPlay;
            }

            au.loadAndPlay(mng, event.getChannel(), toPlay, false);
        }

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public String getName() {
        return "play";
    }

}
