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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission

class VoiceKickCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.name = "voicekick"
        this.help = "Kicks a user from the voice channel that they are in"
        this.usage = "<@user/voice channel>"
        this.userPermissions = arrayOf(Permission.KICK_MEMBERS)
        this.botPermissions = arrayOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER, Permission.VOICE_MOVE_OTHERS)
    }

    override fun execute(ctx: CommandContext) {
        val channels = ctx.guild.getVoiceChannelsByName(ctx.argsRaw, true)
        val guild = ctx.guild

        if (channels.isNotEmpty()) {
            val channel = channels[0]

            channel.createCopy().queue {
                channel.delete().queue()
            }

            sendSuccess(ctx.message)

            return
        }

        val mentioned = ctx.getMentionedArg(0)

        if (mentioned.isNotEmpty()) {
            val member = mentioned[0]

            if (member.voiceState!!.channel == null) {
                sendMsg(ctx, "That member is not in a voice channel")
                return
            }

            guild.kickVoiceMember(member).queue()
            sendSuccess(ctx.message)

            return
        }

        sendMsg(ctx, "I could not find any Voice Channel or member to kick from voice")
    }
}
