package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.PermissionException
import net.dv8tion.jda.core.managers.AudioManager

class JoinCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(chennelChecks(event)) {
            boolean inChannel = false
            VoiceChannel vc = null

            for(VoiceChannel chan : event.guild.voiceChannels) {
                if(chan.members.contains(event.member)) {
                    inChannel = true
                    vc = chan
                    break
                }
            }

            if(!inChannel){
                sendMsg(event, "You are not in a voice channel")
                return
            }
            Guild guild = event.guild
            GuildMusicManager mng = getMusicManager(guild)
            AudioManager audioManager = getAudioManager(guild)
            if(audioManager.connected && mng.player.playingTrack != null){
                sendMsg(event, "I'm already in a channel.")
                return
            }

            try {
                if(audioManager.connected) audioManager.closeAudioConnection()

                audioManager.openAudioConnection(vc)
            } catch (PermissionException e) {
                if(e.getPermission() == Permission.VOICE_CONNECT){
                    sendMsg(event, "I don't have permission to join `"+vc.getName()+"`")
                }
            }

        }
    }

    @Override
    String help() {
        return "makes the bot join the voice channel that you are in."
    }

    @Override
    String getName() {
        return "join"
    }

    @Override
    String[] getAliases() {
        return ["summon", "connect"]
    }
}
