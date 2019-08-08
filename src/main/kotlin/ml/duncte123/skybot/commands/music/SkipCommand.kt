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

import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.extensions.getImageUrl
import ml.duncte123.skybot.objects.ConsoleUser
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import java.util.function.BiFunction

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class SkipCommand : MusicCommand() {

    init {
        this.name = "skip"
        this.aliases = arrayOf("next", "nexttrack", "skiptrack")
        this.helpFunction = BiFunction { _, _ -> "Skips the current track" }
        this.usageInstructions = BiFunction { invoke, prefix -> "`$prefix$invoke [skip count]`" }
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val scheduler = mng.scheduler

        mng.lastChannel = -1

        if (mng.player.playingTrack == null) {
            sendMsg(event, "The player is not playing.")
            return
        }

        val count = if (args.isNotEmpty()) {
            if (!args[0].matches("\\d{1,10}".toRegex())) {
                1
            } else {
                args[0].toInt().coerceIn(1, scheduler.queue.size.coerceAtLeast(1))
            }
        } else {
            1
        }

        repeat(count) {
            scheduler.nextTrack()
        }

        if (mng.player.playingTrack != null) {
            val trackUserData = mng.player.playingTrack.userData

            val user = if (trackUserData != null && trackUserData is TrackUserData) {
                // Return the console user when the requester is null
                ctx.jda.getUserById(trackUserData.userId) ?: ConsoleUser()
            } else {
                ctx.author
            }

            val track = mng.player.playingTrack

            sendEmbed(event, embedMessage("Successfully skipped $count tracks.\n" +
                "Now playing: ${track.info.title}\n" +
                "Requester: ${user.asTag}")
                .setThumbnail(track.getImageUrl())
            )
        } else {
            sendMsg(event, "Successfully skipped $count tracks.\n" +
                "Queue is now empty.")
        }

        mng.lastChannel = event.channel.idLong
    }
}
