package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

public class JoinCommand extends Command {

    public final static String help = "makes the bot join the voice channel that you are in.";

    private String chanId = "";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        boolean inChannel = false;

        for(VoiceChannel chan : event.getGuild().getVoiceChannels()){
            if(chan.getMembers().contains(event.getMember())){
                inChannel = true;
                chanId = chan.getId();
                break;
            }
        }

        if(!inChannel){
            event.getChannel().sendMessage("You are not in a voice channel").queue();
            return;
        }

        VoiceChannel vc = null;
        AudioUtils au = AirUtils.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);


        if(event.getGuild().getAudioManager().isConnected() && !mng.player.getPlayingTrack().equals(null)){
            event.getChannel().sendMessage("I'm already in a channel.").queue();
            return;
        }

        for(VoiceChannel chan : event.getGuild().getVoiceChannels()){
            if(chan.getId().equals(chanId)){
                vc = chan;
                break;
            }
        }


        EmbedBuilder eb = EmbedUtils.defaultEmbed();
        try{
            if(event.getGuild().getAudioManager().isConnected()){
                event.getGuild().getAudioManager().closeAudioConnection();
            }
            vc.getGuild().getAudioManager().openAudioConnection(vc);
            eb.addField("", "Joining `" + vc.getName() + "`.", false);
        }catch(PermissionException e){
            if(e.getPermission() == Permission.VOICE_CONNECT){
                eb.addField("", "I don't have permission to join `"+vc.getName()+"`", false);
            }
        }
        sendEmbed(eb.build(), event);


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
        return "join";
    }
}
