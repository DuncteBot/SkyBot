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

package me.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.utils.sendMessage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.PermissionException
import kotlin.jvm.optionals.getOrNull

class JoinCommand : MusicCommand() {
    init {
        this.name = "join"
        this.aliases = arrayOf("summon", "connect")
        this.help = "Makes the bot join the voice channel that you are in."
        this.cooldown = MUSIC_COOLDOWN
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val member = ctx.member
        val voiceState = member.voiceState!!

        if (!voiceState.inAudioChannel()) {
            sendMsg(ctx, "Please join a voice channel first.")
            return
        }

        val vc = voiceState.channel!!
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)

        mng.latestChannelId = event.channel.idLong

        val lavalink = getLavalinkManager()
        val player = mng.player.getOrNull()

        if (player != null) {
            if (lavalink.isConnected(event.guild) && player.track != null) {
                val channel = lavalink.getConnectedChannel(event.guild)

                if (channel == null) {
                    sendMsg(ctx, "I am already playing music in a channel, but somehow discord did not tell me what channel I am in.")
                    return
                }

                val channelId = channel.idLong

                sendMsg(ctx, "I am already playing music in <#$channelId>.")
                return
            }

            if (!ctx.selfMember.hasPermission(vc, Permission.VOICE_CONNECT)) {
                sendMsg(ctx, "I cannot join to <#${vc.idLong}> because I am missing the permission to do so.")

                return
            }
        }

        try {
            lavalink.openConnection(vc)
            sendMsg(ctx, "Connected to <#${vc.idLong}>")
        } catch (e: PermissionException) {
            if (e.permission == Permission.VOICE_CONNECT) {
                sendMsg(ctx, "I don't have permission to join <#${vc.idLong}>")
            } else {
                sendMsg(ctx, "Error while joining channel <#${vc.idLong}>: ${e.message}")
            }
        } catch (other: Exception) {
            sendErrorWithMessage(ctx.message, "Could not join channel: ${other.message}")
        }
    }

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        val memberVoice = event.member!!.voiceState!!

        if (!memberVoice.inAudioChannel()) {
            event.reply("Please join a voice channel first.").queue()
            return
        }

        val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)

        mng.latestChannelId = event.channel.idLong

        val lavalink = getLavalinkManager()
        val player = mng.player.getOrNull()
        val vc = memberVoice.channel!!

        if (player != null) {
            if (lavalink.isConnected(event.guild) && player.track != null) {
                val channel = lavalink.getConnectedChannel(event.guild!!)

                if (channel == null) {
                    event.sendMessage(
                        "I am already playing music in a channel, but somehow discord did not tell me what channel I am in."
                    )
                    return
                }

                event.sendMessage(
                    "I am already playing music in ${channel.asMention}."
                )
                return
            }

            if (!event.guild!!.selfMember.hasPermission(vc, Permission.VOICE_CONNECT)) {
                event.sendMessage(
                    "I cannot join to ${vc.asMention} because I am missing the permission to do so."
                )

                return
            }
        }

        try {
            lavalink.openConnection(vc)
            event.sendMessage(
                "Connected to ${vc.asMention}"
            )
        } catch (e: PermissionException) {
            if (e.permission == Permission.VOICE_CONNECT) {
                event.sendMessage(
                    "I don't have permission to join ${vc.asMention}"
                )
            } else {
                event.sendMessage(
                    "Error while joining channel ${vc.asMention}: ${e.message}"
                )
            }
        } catch (other: Exception) {
            event.sendMessage(
                "Could not join channel: ${other.message}"
            )
        }
    }
}
