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

import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.ILoveStream
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import java.awt.Color

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class NowPlayingCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!channelChecks(event, ctx.audioUtils))
            return
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player
        val msg = when {
            player.playingTrack != null && !player.playingTrack.info.isStream ->
                EmbedUtils.embedMessage("**Playing** [${player.playingTrack.info.title}](${player.playingTrack.info.uri})\n" + EmbedUtils.playerEmbed(mng))
            player.playingTrack != null && player.playingTrack.info.isStream -> {
                val json = WebUtils.ins.getJSONObject("https://www.iloveradio.de/typo3conf/ext/ep_channel/Scripts/playlist.php").execute()
                val stream = (ctx.commandManager.getCommand("radio") as RadioCommand).radioStreams.first { it.url == player.playingTrack.info.uri }
                if (stream is ILoveStream) {
                    val channeldata = json!!.getJSONObject("channel-${stream.npChannel}")
                    EmbedUtils.defaultEmbed().setDescription("**Playing [${channeldata.getString("title")}](${stream.url}) by ${channeldata.getString("artist")}**")
                            .setThumbnail("https://www.iloveradio.de${channeldata.getString("cover")}").setColor(Color.decode(channeldata.getString("color"))).build()
                } else {
                    EmbedUtils.embedMessage("**Playing [${stream.name}](${stream.url})")
                }
            }
            else -> EmbedUtils.embedMessage("The player is not currently playing anything!")
        }
        sendEmbed(event, msg)
    }

    override fun help(): String = "Prints information about the currently playing song (title, current time)"

    override fun getName(): String = "nowplaying"

    override fun getAliases(): Array<String> = arrayOf("np", "song")
}