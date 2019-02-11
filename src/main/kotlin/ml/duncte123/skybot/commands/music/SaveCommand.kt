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

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import org.json.JSONArray
import java.nio.charset.StandardCharsets.UTF_8

@Author(nickname = "ramidzkh", author = "Ramid Khan")
class SaveCommand : MusicCommand() {

    override fun run(ctx: CommandContext) {

        val event = ctx.event

        event.channel.sendFile(
            toByteArray(event.guild, ctx.audioUtils),
            "playlist.json",
            MessageBuilder()
                .append(event.author)
                .append(", here is the queue which can be re-imported")
                .build()).queue()
    }

    private fun toByteArray(guild: Guild?, audioUtils: AudioUtils): ByteArray {
        val array = JSONArray()
        val manager = getMusicManager(guild, audioUtils)

        val urls = manager.scheduler.queue
            .map { it.identifier }
            .toMutableList()

        if (manager.player.playingTrack != null) {
            urls.add(0, manager.player.playingTrack.identifier)
        }

        for (url in urls) {
            array.put(url)
        }

        return array.toString(2).toByteArray(UTF_8)
    }

    override fun getName() = "save"

    override fun help() = "Saves a playlist into a file with can be loaded with ${Settings.PREFIX}load"
}
