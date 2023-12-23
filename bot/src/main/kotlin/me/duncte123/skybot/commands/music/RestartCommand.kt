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
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RestartCommand : MusicCommand() {
    init {
        this.name = "restart"
        this.help = "Start the current track from the beginning"
    }

    override fun run(ctx: CommandContext) {
        val player = ctx.audioUtils.getMusicManager(ctx.guildId).player
        val currentTrack = player.currentTrack

        if (currentTrack == null) {
            sendError(ctx.message)
            sendMsg(ctx, "No track currently playing")
            return
        }

        if (!currentTrack.info.isSeekable) {
            sendMsg(ctx, "This track is not seekable")
            return
        }

        player.seekTo(0)

        sendSuccess(ctx.message)
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue()
    }
}
