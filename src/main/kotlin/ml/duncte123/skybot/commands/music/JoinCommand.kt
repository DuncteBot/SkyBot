/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.exceptions.PermissionException
import java.util.function.BiFunction

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class JoinCommand : MusicCommand() {

    init {
        this.name = "join"
        this.aliases = arrayOf("summon", "connect")
        this.helpFunction = BiFunction { _, _ -> "Makes the bot join the voice channel that you are in." }
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event

        if (!event.member.voiceState.inVoiceChannel()) {
            sendMsg(event, "Please join a voice channel first.")
            return
        }

        val vc = event.member.voiceState.channel
        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)

        mng.lastChannel = event.channel.idLong

        if (hasCoolDown(guild) && !isUserOrGuildPatron(event, false)) {
            sendMsg(event, """I still have cooldown!
                    |Remaining cooldown: ${cooldowns[guild.idLong].toDouble() / 1000}s""".trimMargin())
            sendError(event.message)
            return
        }

        cooldowns.remove(guild.idLong)

        val lavalink = getLavalinkManager()

        if (lavalink.isConnected(event.guild) && mng.player.playingTrack != null) {
            sendMsg(event, "I'm already in a channel.")
            return
        }

        if (!ctx.selfMember.hasPermission(vc, Permission.VOICE_CONNECT)) {
            sendMsg(event, "I cannot connect to <#${vc.id}>")

            return
        }

        try {
            lavalink.openConnection(vc)
            addCooldown(guild.idLong)
            sendSuccess(event.message)
        } catch (e: PermissionException) {
            if (e.permission == Permission.VOICE_CONNECT) {
                sendMsg(event, "I don't have permission to join `${vc?.name}`")
            } else {
                sendMsg(event, "Error while joining channel `${vc?.name}`: ${e.message}")
            }
        }

    }
}
