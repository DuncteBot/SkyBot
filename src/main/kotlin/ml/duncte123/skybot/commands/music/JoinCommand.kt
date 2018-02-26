/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.commands.uncategorized.OneLinerCommands
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audio.hooks.ConnectionListener
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.exceptions.PermissionException

@Author(nickname = "Sanduhr32", author = "Maurice R S")


class JoinCommand : MusicCommand(), ConnectionListener {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if (!event.member.voiceState.inVoiceChannel()) {
            MessageUtils.sendMsg(event, "Please join a voice channel first.")
            return
        }

        val vc = event.member.voiceState.channel
        val guild = event.guild
        val mng = getMusicManager(guild)
        mng.latestChannel = event.channel

        @Suppress("DEPRECATION")
        if (cooldowns.containsKey(guild.idLong) && cooldowns[guild.idLong] > 0 && !(Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id) || event.author.id == Settings.OWNER_ID)) {
            MessageUtils.sendMsg(event, """I still have cooldown!
                    |Remaining cooldown: ${cooldowns[guild.idLong].toDouble() / 1000}s""".trimMargin())
            MessageUtils.sendError(event.message)
            return
        }
        cooldowns.remove(guild.idLong)

        if (getLavalinkManager().isConnected(event.guild) && mng.player.playingTrack != null) {
            MessageUtils.sendMsg(event, "I'm already in a channel.")
            return
        }
        try {
            if(guild.audioManager.connectionListener == null)
                guild.audioManager.connectionListener = this
            getLavalinkManager().openConnection(vc)
            MusicCommand.addCooldown(guild.idLong)
            MessageUtils.sendSuccess(event.message)
        } catch (e: PermissionException) {
            if (e.permission == Permission.VOICE_CONNECT) {
                MessageUtils.sendMsg(event, "I don't have permission to join `${vc?.name}`")
            } else {
                MessageUtils.sendMsg(event, "Error while joining channel `${vc?.name}`: ${e.message}")
            }
        }
        catch (e1: Exception) {
            e1.printStackTrace()
        }

    }

    override fun help(): String = "Makes the bot join the voice channel that you are in."

    override fun getName(): String = "join"

    override fun getAliases(): Array<String> = arrayOf("summon", "connect")

    //Audio stuff
    override fun onStatusChange(p0: ConnectionStatus?) { /* Unused */ }

    override fun onUserSpeaking(p0: User?, p1: Boolean) { /* Unused */ }
    //Listen for ping
    override fun onPing(ping: Long) {
        OneLinerCommands.pingHistory.add(ping, true)
    }
}