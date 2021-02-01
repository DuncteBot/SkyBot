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

import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils.getDatabaseDateFormat
import ml.duncte123.skybot.utils.AudioUtils
import net.dv8tion.jda.api.entities.Guild
import java.time.OffsetDateTime

class SaveCommand : MusicCommand() {

    init {
        this.name = "save"
        this.help = "Saves a playlist into a file with can be loaded with `{prefix}load`"
    }

    override fun run(ctx: CommandContext) {
        ctx.channel.sendMessage("${ctx.author.asTag}, here is the queue which can be re-imported with `${ctx.prefix}load`")
            .addFile(
                toByteArray(ctx.guild, ctx.audioUtils, ctx.variables.jackson),
                "playlist-${getDatabaseDateFormat(OffsetDateTime.now())}.json"
            )
            .queue()
    }

    private fun toByteArray(guild: Guild, audioUtils: AudioUtils, mapper: ObjectMapper): ByteArray {
        val array = mapper.createArrayNode()
        val manager = audioUtils.getMusicManager(guild)

        val urls = manager.scheduler.queue
            .map { it.info.uri }
            .toMutableList()

        if (manager.player.playingTrack != null) {
            urls.add(0, manager.player.playingTrack.info.uri)
        }

        for (url in urls) {
            array.add(url)
        }

        return mapper.writeValueAsBytes(array)
    }
}
