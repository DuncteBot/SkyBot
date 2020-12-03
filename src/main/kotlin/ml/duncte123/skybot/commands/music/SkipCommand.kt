/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings.NO_STATIC
import ml.duncte123.skybot.Settings.YES_STATIC
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import kotlin.math.ceil

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class SkipCommand : MusicCommand() {

    init {
        this.name = "skip"
        this.aliases = arrayOf("next", "nexttrack", "skiptrack")
        this.help = "Skips the current track"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guild)
        val player = mng.player

        if (player.playingTrack == null) {
            sendMsg(ctx, "The player is not playing.")
            return
        }

        if (!player.playingTrack.isSeekable) {
            sendMsg(ctx, "This track is not seekable")
            return
        }

        val author = ctx.author
        val trackData = player.playingTrack.getUserData(TrackUserData::class.java)

        if (trackData.requester == author.idLong) {
            doSkip(ctx, mng)
            return
        }

        val listeners = getLavalinkManager()
            .getConnectedChannel(ctx.guild)
            .members.filter {
                !it.user.isBot && !(it.voiceState?.isDeafened ?: false)
            }.count()

        val votes = trackData.votes

        var msg = if (votes.contains(author.idLong)) {
            "$NO_STATIC You already voted to skip this song `["
        } else {
            votes.add(author.idLong)
            "$YES_STATIC Successfully voted to skip the song `["
        }

        val skippers = getLavalinkManager().getConnectedChannel(ctx.guild)
            .members.filter { votes.contains(it.idLong) }.count()
        val required = ceil(listeners * .55).toInt()

        msg += "$skippers votes, $required/$listeners needed]`"

        sendMsg(ctx, msg)

        if (skippers >= required) {
            doSkip(ctx, mng)
        }
    }

    private fun doSkip(ctx: CommandContext, mng: GuildMusicManager) {
        val player = mng.player

//        player.seekTo(player.playingTrack.duration)

        mng.scheduler.specialSkipCase()

        if (player.playingTrack == null) {
            sendMsg(
                ctx,
                "Successfully skipped the track.\n" +
                    "Queue is now empty."
            )
        }
    }
}
