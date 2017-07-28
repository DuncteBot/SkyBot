package me.duncte123.skybot.commands.music;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.audio.GuildMusicManager;
import me.duncte123.skybot.utils.AudioUtils;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class JoinCommand extends ListenerAdapter implements Command {

    public final static String help = "makes the bot join the voice channel that you are in.";

    private String chanId = "";

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        boolean inChannel = false;

        for(VoiceChannel chan : event.getGuild().getVoiceChannels()){
            if(chan.getMembers().contains(event.getMember())){
                inChannel = true;
                chanId = chan.getId();
                break;
            }
        }

        if(!inChannel){
            event.getTextChannel().sendMessage("You are not in a voice channel").queue();
        }

        return inChannel;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        VoiceChannel vc = null;
        AudioUtils au = SkyBot.au;

        Guild guild = event.getGuild();
        GuildMusicManager mng = au.getMusicManager(guild);


        if(event.getGuild().getAudioManager().isConnected() && !mng.player.getPlayingTrack().equals(null)){
            event.getTextChannel().sendMessage(AirUtils.embedMessage("I'm already in a channel.")).queue();
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
        event.getTextChannel().sendMessage(eb.build()).queue();


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
