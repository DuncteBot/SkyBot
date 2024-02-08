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

package me.duncte123.skybot.commands.music

import com.dunctebot.models.utils.DateUtils.getDatabaseDateFormat
import com.fasterxml.jackson.databind.ObjectMapper
import me.duncte123.skybot.Variables
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.utils.AudioUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

class SaveCommand : MusicCommand() {
    init {
        this.name = "save"
        this.help = "Saves a playlist into a file with can be loaded with `{prefix}load`"
    }

    override fun run(ctx: CommandContext) {
        ctx.channel.sendMessage("${ctx.author.asTag}, here is the queue which can be re-imported with `${ctx.prefix}load`")
            .addFiles(
                FileUpload.fromData(
                    toByteArray(ctx.guildId, ctx.audioUtils, ctx.variables.jackson),
                    "playlist-${getDatabaseDateFormat(ZonedDateTime.now())}.json"
                )
            )
            .queue()
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue()
    }

    private fun toByteArray(guildId: Long, audioUtils: AudioUtils, mapper: ObjectMapper): ByteArray {
        val array = mapper.createArrayNode()
        val manager = audioUtils.getMusicManager(guildId)

        val urls = manager.scheduler.queue
            .map {
                it.info.uri
            }
            .toMutableList()

        val currentTrack = manager.player.getOrNull()?.track

        if (currentTrack != null) {
            urls.add(0, currentTrack.info.uri)
        }

        for (url in urls) {
            array.add(url)
        }

        return mapper.writeValueAsBytes(array)
    }
}
