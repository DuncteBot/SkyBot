/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.musicJava;

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.managers.AudioManager;

public class JoinCommand extends MusicCommand {

    public final static String help = "makes the bot join the voice channel that you are in.";

    private String chanId = "";

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

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

        Guild guild = event.getGuild();
        GuildMusicManager mng = getMusicManager(guild);
        AudioManager audioManager = getAudioManager(guild);


        if(audioManager.isConnected() && mng.player.getPlayingTrack() != null){
            event.getChannel().sendMessage("I'm already in a channel.").queue();
            return;
        }

        for(VoiceChannel chan : event.getGuild().getVoiceChannels()){
            if(chan.getId().equals(chanId)){
                vc = chan;
                break;
            }
        }
        try{
            if(audioManager.isConnected()){
                audioManager.closeAudioConnection();
            }
            audioManager.openAudioConnection(vc);
           sendMsg(event, "Joining `" + vc.getName() + "`.");
        }catch(PermissionException e){
            if(e.getPermission() == Permission.VOICE_CONNECT){
                sendMsg(event, "I don't have permission to join `"+vc.getName()+"`");
            }
        }


    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"summon", "connect"};
    }
}
