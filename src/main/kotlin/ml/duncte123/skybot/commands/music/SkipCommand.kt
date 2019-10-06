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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings.YES_STATIC
import ml.duncte123.skybot.Settings.NO_STATIC
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.extensions.getImageUrl
import ml.duncte123.skybot.objects.ConsoleUser
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import java.util.function.BiFunction
import kotlin.math.ceil

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class SkipCommand : MusicCommand() {

    init {
        this.name = "skip"
        this.aliases = arrayOf("next", "nexttrack", "skiptrack")
        this.helpFunction = BiFunction { _, _ -> "Skips the current track" }
    }

    override fun run(ctx: CommandContext) {
        val mng = getMusicManager(ctx.guild, ctx.audioUtils)
        val author = ctx.author

        if (mng.player.playingTrack == null) {
            sendMsg(ctx, "The player is not playing.")
            return
        }

        val trackData = mng.player.playingTrack.userData as TrackUserData

        if (trackData.requester == author.idLong) {
            doSkip(ctx, mng, trackData)
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
            doSkip(ctx, mng, trackData)
        }
    }

    private fun doSkip(ctx: CommandContext, mng: GuildMusicManager, trackData: TrackUserData) {
        mng.scheduler.skipTrack()

        // Return the console user if the requester is null
        val user = ctx.jda.getUserById(trackData.requester) ?: ConsoleUser()

        val track: AudioTrack? = mng.player.playingTrack

        if (track == null) {
            sendMsg(ctx, "Successfully skipped the track.\n" +
                "Queue is now empty.")
            return
        }

        sendEmbed(ctx, embedMessage("Successfully skipped the track.\n" +
            "Now playing: ${track.info.title}\n" +
            "Requester: ${user.asTag}")
            .setThumbnail(track.getImageUrl()))
    }
}
