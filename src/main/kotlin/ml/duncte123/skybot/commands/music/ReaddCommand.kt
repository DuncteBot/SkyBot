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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.exceptions.LimitReachedException
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.CommandUtils.isUserTagPatron

@Author(nickname = "ramidzkh", author = "Ramid Khan")
class ReaddCommand : MusicCommand() {

    init {
        this.name = "readd"
        this.helpFunction = { _, _ -> "Adds the currently playing track to the end of the queue" }
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val manager = getMusicManager(event.guild, ctx.audioUtils)
        val t = manager.player.playingTrack

        if (t == null) {
            sendError(event.message)
            sendMsg(event, "No tracks in queue")
            return
        }

        val track = t.makeClone()
        track.userData = t.getUserData(TrackUserData::class.java).copy()

        // This is from AudioUtils.java but in Kotlin
        var title = track.info.title
        if (track.info.isStream) {
            val stream = (ctx.commandManager.getCommand("radio") as RadioCommand)
                .radioStreams.stream().filter { s -> s.url == track.info.uri }.findFirst()
            if (stream.isPresent) {
                title = stream.get().name
            }
        }
        var msg = "Adding to queue: $title"
        if (manager.player.playingTrack == null) {
            msg += "\nand the Player has started playing;"
        }

        try {
            manager.scheduler.queue(track, isUserTagPatron(ctx.author))
            sendSuccess(event.message)
            sendEmbed(event.channel, EmbedUtils.embedField(AudioUtils.EMBED_TITLE, msg))
        } catch (e: LimitReachedException) {
            sendMsg(ctx, "You exceeded the maximum queue size of ${e.size} tracks")
        }
    }
}
