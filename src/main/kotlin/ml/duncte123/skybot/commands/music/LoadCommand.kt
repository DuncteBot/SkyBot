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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import java.util.*

class LoadCommand : MusicCommand() {
    init {
        this.mayAutoJoin = true
        this.name = "load"
        this.help = "Loads the given playlist file\nThe playlist can be exported with `{prefix}save`"
    }

    override fun run(ctx: CommandContext) {
        val mapper = ctx.variables.jackson
        val attachments = ctx.message.attachments

        if (attachments.size == 0) {
            sendError(ctx.message)
            sendMsg(ctx, "No attachment given")
            return
        }

        if (attachments.size > 1) {
            sendError(ctx.message)
            sendMsg(ctx, "Please only attach one file at a time")
            return
        }

        val attachment = attachments[0]

        attachment.retrieveInputStream().thenAcceptAsync {
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                val node = mapper.readTree(it)

                if (!node.isArray) {
                    sendMsg(ctx, "Provided file is not a valid JSON array")

                    return@thenAcceptAsync
                }

                val array = node as ArrayNode
                var shouldAnnounce = true

                sendMsg(ctx, "Loading ${array.size()} tracks, please wait...")

                array.filter(Objects::nonNull)
                    .forEach { obj ->
                        // This probably announces it to the channel
                        ctx.audioUtils.loadAndPlay(
                            ctx,
                            (obj as JsonNode).asText(),
                            shouldAnnounce
                        ).get()

                        shouldAnnounce = false
                    }

                sendEmbed(
                    ctx,
                    EmbedUtils.embedField(
                        AudioUtils.EMBED_TITLE,
                        "Added ${array.size()} requested tracks."
                    )
                )
            } catch (exception: Exception) {
                sendError(ctx.message)
                sendMsg(ctx, "Invalid JSON file!")
            } finally {
                it.close()
            }
        }
    }
}
