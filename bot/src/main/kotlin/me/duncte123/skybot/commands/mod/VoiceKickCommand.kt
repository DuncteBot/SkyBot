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
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

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

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addOptions(
            OptionData(
                OptionType.CHANNEL,
                "voice_channel",
                "The voice channel to kick ALL users from.",
                false
            ),
            OptionData(
                OptionType.USER,
                "user",
                "The user to kick from the voice channel they are currently in.",
                false
            ),
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        val vc = event.getOption("voice_channel")?.asChannel

        if (vc != null) {
            if (vc.type == ChannelType.VOICE) {
                event.reply("Kicking all users from `${vc.name}`, please wait...")
                    .setEphemeral(false)
                    .queue()

                vc.asVoiceChannel().createCopy().queue {
                    vc.delete().reason("Kicking all users from voice (${event.user.asTag})").queue()

                    event.hook.editOriginal("Kicked all users from `${vc.name}`").queue()
                }
                return
            }

            event.reply("Can't voice kick from a text channel, sorry")
                .setEphemeral(true)
                .queue()
            return
        }

        val member = event.getOption("user")?.asMember

        if (member != null) {
            // We have to "== true" here because the return type is a nullable boolean
            if (member.voiceState?.inAudioChannel() == true) {
                event.guild!!.kickVoiceMember(member).queue()
                event.reply("Kicked ${member.user.asTag} from voice").setEphemeral(false).queue()
                return
            }

            event.reply("That user is not in a voice channel")
                .setEphemeral(true)
                .queue()

            return
        }

        event.reply("Please either specify a voice channel or a user to kick from the voice channel.")
            .queue()
    }
}
