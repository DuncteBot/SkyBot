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
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONTokener
import java.util.*

@Author(nickname = "ramidzkh", author = "Ramid Khan")
class LoadCommand : MusicCommand() {

    init {
        this.withAutoJoin = true
    }

    override fun run(ctx: CommandContext) {

        val event = ctx.event
        val attachments = event.message.attachments

        if (attachments.size == 0) {
            sendError(event.message)
            sendMsg(event, "No attachment given")
            return
        }

        if (attachments.size > 1) {
            sendError(event.message)
            sendMsg(event, "Please only attach one file at a time")
            return
        }

        val attachment = attachments[0]

        attachment.withInputStream {
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                val array = JSONArray(JSONTokener(it))
                val musicManager = getMusicManager(event.guild, ctx.audioUtils)
                var shouldAnnounce = true

                sendMsg(event, "Loading ${array.length()} tracks, please wait...")

                array.filter(Objects::nonNull)
                    .forEach { obj ->
                        // This probably announces it to the channel
                        ctx.audioUtils.loadAndPlay(musicManager,
                            obj.toString(),
                            false,
                            ctx,
                            shouldAnnounce).get()

                        shouldAnnounce = false
                    }

                sendEmbed(event, EmbedUtils.embedField(ctx.audioUtils.embedTitle,
                    "Added ${array.length()} requested tracks."))
            } catch (exception: JSONException) {
                sendError(event.message)
                sendMsg(event, "Invalid JSON file!")
            }
        }
    }

    override fun getName() = "load"

    override fun help() = "Loads a given playlist file"
}
