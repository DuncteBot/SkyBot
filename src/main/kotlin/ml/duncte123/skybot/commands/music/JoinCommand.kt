/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.PermissionException

@Author(nickname = "Sanduhr32", author = "Maurice R S")


class JoinCommand : MusicCommand() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if (!event.member.voiceState.inVoiceChannel()) {
            sendMsg(event, "Please join a voice channel first.")
            return
        }
        val vc = event.member.voiceState.channel
        val guild = event.guild
        val mng = getMusicManager(guild)
        val audioManager = getAudioManager(guild)
        if (audioManager.isConnected && mng.player.playingTrack != null) {
            sendMsg(event, "I'm already in a channel.")
            return
        }

        try {
            if (audioManager.isConnected)
                audioManager.closeAudioConnection()

            audioManager.openAudioConnection(vc)

            sendSuccess(event.message)
        } catch (e: PermissionException) {
            if (e.permission == Permission.VOICE_CONNECT) {
                sendMsg(event, "I don't have permission to join `${vc.name}`")
            }
        }

    }

    override fun help(): String = "Makes the bot join the voice channel that you are in."

    override fun getName(): String = "join"

    override fun getAliases(): Array<String> = arrayOf("summon", "connect")
}