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
 *
 */

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.PermissionException

class JoinCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        boolean inChannel = false
        def vc = null

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
        def guild = event.guild
        def mng = getMusicManager(guild)
        def audioManager = getAudioManager(guild)
        if(audioManager.connected && mng.player.playingTrack != null){
            sendMsg(event, "I'm already in a channel.")
            return
        }

        try {
            if(audioManager.connected) audioManager.closeAudioConnection()

            audioManager.openAudioConnection(vc)
        } catch (PermissionException e) {
            if(e.permission == Permission.VOICE_CONNECT){
                sendMsg(event, "I don't have permission to join `${vc.name}`")
            }
        }

    }

    @Override
    String help() {
        return "Makes the bot join the voice channel that you are in."
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
