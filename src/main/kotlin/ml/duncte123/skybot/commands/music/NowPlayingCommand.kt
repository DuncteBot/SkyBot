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
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.ILoveStream
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.MusicEmbedUtils.playerEmbed
import java.awt.Color

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class NowPlayingCommand : MusicCommand() {

    override fun executeCommand(ctx: CommandContext) {
        val event = ctx.event
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player

        val msg = when {
            player.playingTrack != null && !player.playingTrack.info.isStream ->
                embedMessage("**Playing** [${player.playingTrack.info.title}](${player.playingTrack.info.uri})\n" + playerEmbed(mng))

            player.playingTrack != null && player.playingTrack.info.isStream -> {
                val trackinfo = player.playingTrack.info
                val stream = (ctx.commandManager.getCommand("radio") as RadioCommand)
                    .radioStreams.first { it.url == trackinfo.uri }

                if (stream is ILoveStream) {
                    val json = WebUtils.ins
                        .getJSONObject("https://www.iloveradio.de/typo3conf/ext/ep_channel/Scripts/playlist.php").execute()
                    val channelData = json.getJSONObject("channel-${stream.npChannel}")

                    EmbedUtils.defaultEmbed().setDescription("**Playing [${channelData.getString("title")}]" +
                        "(${stream.url}) by ${channelData.getString("artist")}**")
                        .setThumbnail("https://www.iloveradio.de${channelData.getString("cover")}")
                        .setColor(Color.decode(channelData.getString("color")))
                } else {
                    embedMessage("**Playing [${trackinfo.title}](${trackinfo.uri})")
                }

            }

            else -> embedMessage("The player is not currently playing anything!")
        }

        sendEmbed(event, msg)
    }

    override fun help(): String = "Prints information about the currently playing song (title, current time)"

    override fun getName(): String = "nowplaying"

    override fun getAliases(): Array<String> = arrayOf("np", "song")
}
