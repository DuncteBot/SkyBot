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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.getImageUrl
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.objects.user.ConsoleUser
import net.dv8tion.jda.api.Permission

class ForceSkip : MusicCommand() {

    init {
        this.name = "forceskip"
        this.aliases = arrayOf("modskip")
        this.help = "Force skips the current track"
        this.usage = "[skip count]"
        this.userPermissions = arrayOf(
            Permission.MANAGE_SERVER
        )
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guild)
        val player = mng.player

        if (player.playingTrack == null) {
            sendMsg(ctx, "The player is not playing.")
            return
        }

        val args = ctx.args
        val scheduler = mng.scheduler

        val count = if (args.isNotEmpty()) {
            if (!args[0].matches("\\d{1,10}".toRegex())) {
                1
            } else {
                args[0].toInt().coerceIn(1, scheduler.queue.size.coerceAtLeast(1))
            }
        } else {
            1
        }

        val trackData = player.playingTrack.getUserData(TrackUserData::class.java)

        // https://github.com/jagrosh/MusicBot/blob/master/src/main/java/com/jagrosh/jmusicbot/commands/music/SkipCmd.java
        scheduler.skipTracks(count)

        // Return the console user if the requester is null
        val user = ctx.jda.getUserById(trackData.requester) ?: ConsoleUser()

        val track: AudioTrack? = player.playingTrack

        if (track == null) {
            sendMsg(
                ctx,
                "Successfully skipped $count tracks.\n" +
                    "Queue is now empty."
            )
            return
        }

        sendEmbed(
            ctx,
            EmbedUtils.embedMessage(
                "Successfully skipped $count tracks.\n" +
                    "Now playing: ${track.info.title}\n" +
                    "Requester: ${user.asTag}"
            )
                .setThumbnail(track.getImageUrl())
        )
    }
}
