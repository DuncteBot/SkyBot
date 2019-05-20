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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.VoiceChannel

@Author(nickname = "duncte123", author = "Duncan Sterken")
class VoiceKickCommand : ModBaseCommand() {

    init {
        this.perms = arrayOf(Permission.KICK_MEMBERS)
        this.selfPerms = arrayOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_SERVER, Permission.VOICE_MOVE_OTHERS)
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val channels = ctx.guild.getVoiceChannelsByName(ctx.argsRaw, true)
        val controller = ctx.guild.controller

        if (channels.isNotEmpty()) {
            val channel = channels[0]

            channel.createCopy().queue {
                channel.delete().queue()
            }

            sendSuccess(ctx.message)

            return
        }

        if (ctx.message.mentionedMembers.isNotEmpty()) {
            val member = ctx.message.mentionedMembers[0]

            if (member.voiceState.channel == null) {
                sendMsg(event, "That member is not in a voice channel")
                return
            }

            controller.createVoiceChannel("temp_voicekick_${System.currentTimeMillis()}").queue { channel ->
                if (channel !is VoiceChannel) {
                    logger.error("Created a Voice Channel but the result wasn't a voice channel (received ${channel.type.name.toLowerCase()})")

                    return@queue
                }

                controller.moveVoiceMember(member, channel).queue {
                    channel.delete().queue()
                }
            }

            sendSuccess(ctx.message)

            return
        }

        sendMsg(event, "I could not find any Voice Channel or member to kick from voice")
    }

    override fun getName() = "voicekick"

    override fun help() = """Kicks a user from the voice channel
        |Usage: `${Settings.PREFIX}$name <@user/voice channel>`
    """.trimMargin()
}
