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

package ml.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.exceptions.LimitReachedException
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.CommandUtils.isUserTagPatron

class ReaddCommand : MusicCommand() {

    init {
        this.name = "readd"
        this.help = "Adds the currently playing track to the end of the queue"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val manager = ctx.audioUtils.getMusicManager(event.guild)
        val track = manager.player.playingTrack

        if (track == null) {
            sendError(event.message)
            sendMsg(ctx, "No tracks in queue")
            return
        }

        val clone = track.makeClone()
        clone.userData = track.getUserData(TrackUserData::class.java).copy()

        // This is from AudioUtils.java but in Kotlin
        var title = clone.info.title
        if (clone.info.isStream) {
            val stream = (ctx.commandManager.getCommand("radio") as RadioCommand)
                .radioStreams.stream().filter { s -> s.url == clone.info.uri }.findFirst()
            if (stream.isPresent) {
                title = stream.get().name
            }
        }
        var msg = "Adding to queue: $title"
        if (manager.player.playingTrack == null) {
            msg += "\nand the Player has started playing;"
        }

        try {
            manager.scheduler.addToQueue(clone, isUserTagPatron(ctx.author))
            sendSuccess(event.message)
            sendEmbed(ctx, EmbedUtils.embedField(AudioUtils.EMBED_TITLE, msg))
        } catch (e: LimitReachedException) {
            sendMsg(ctx, "You exceeded the maximum queue size of ${e.size} tracks")
        }
    }
}
