/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class NowPlayingCommand : MusicCommand() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return
        val mng = getMusicManager(event.guild)
        mng.latestChannel = event.channel
        val player = mng.player
        val msg = if (player.playingTrack != null) {
            "**Playing** [${player.playingTrack.info.title}](${player.playingTrack.info.uri})\n" + EmbedUtils.playerEmbed(mng)
        } else {
            "The player is not currently playing anything!"
        }
        MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(msg))
    }

    override fun help(): String = "Prints information about the currently playing song (title, current time)"

    override fun getName(): String = "nowplaying"

    override fun getAliases(): Array<String> = arrayOf("np", "song")
}