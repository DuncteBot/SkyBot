package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
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
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
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
        }

        return inChannel;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        VoiceChannel vc = null;
        AudioUtils au = SkyBot.au;

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


        EmbedBuilder eb = AirUtils.defaultEmbed();
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
        event.getChannel().sendMessage(eb.build()).queue();


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
}
