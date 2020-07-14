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
import ml.duncte123.skybot.objects.TrackUserData
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.Permission

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class StopCommand : MusicCommand() {

    init {
        this.name = "stop"
        this.help = "Stops the music"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)
        val player = mng.player
        val track = player.playingTrack

        if (track == null) {
            sendMsg(event, "The player is not playing.")
            return
        }

        val trackData = track.getUserData(TrackUserData::class.java)

        if (ctx.guild.settings.isAllowAllToStop || trackData.requester == ctx.author.idLong || ctx.member.hasPermission(Permission.MANAGE_SERVER)) {
            mng.scheduler.queue.clear()
            player.stopTrack()
            player.isPaused = false

            sendMsg(event, "Playback has been completely stopped and the queue has been cleared.")

            return
        }

        sendMsg(event, "Only the person that started this track " +
            "or people with the `Manage Server` permission can stop this track")
    }
}
