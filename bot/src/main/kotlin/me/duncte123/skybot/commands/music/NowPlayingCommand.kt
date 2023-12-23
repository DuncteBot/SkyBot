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

import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.skybot.Variables
import me.duncte123.skybot.extensions.toEmbed
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class NowPlayingCommand : MusicCommand() {
    init {
        this.name = "nowplaying"
        this.aliases = arrayOf("np", "song")
        this.help = "Prints information about the currently playing song (title, current time)"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        val player = mng.player
        val currentTrack = player.currentTrack

        if (currentTrack == null) {
            sendEmbed(
                ctx,
                embedMessage("The player is not currently playing anything!")
            )
            return
        }

        currentTrack.toEmbed(mng, ctx.shardManager) { message ->
            sendEmbed(ctx, message)
        }
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)
        val player = mng.player
        val currentTrack = player.currentTrack

        if (currentTrack == null) {
            event.replyEmbeds(
                embedMessage("The player is not currently playing anything!").build()
            ).queue()
            return
        }

        currentTrack.toEmbed(mng, event.jda.shardManager!!) { message ->
            event.replyEmbeds(message.build()).queue()
        }
    }
}
