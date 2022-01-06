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

package ml.duncte123.skybot.commands.essentials

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils

class LLRwCommand : MusicCommand() {
    init {
        this.mayAutoJoin = true
        this.category = CommandCategory.UNLISTED
        this.name = "llrw"
    }

    override fun run(ctx: CommandContext) {
        if (!CommandUtils.isDev(ctx.author)) {
            return
        }

        val link = getLavalinkManager().lavalink.getLink(ctx.guild)

        link.restClient.loadItem(ctx.argsRaw, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                sendMsg(ctx, "Track loaded")

                link.player.playTrack(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                TODO("Not yet implemented")
            }

            override fun noMatches() {
                sendMsg(ctx, "no matches")
            }

            override fun loadFailed(exception: FriendlyException) {
                sendMsg(ctx, "Load failed ${exception.message}")
            }

        })
    }
}
