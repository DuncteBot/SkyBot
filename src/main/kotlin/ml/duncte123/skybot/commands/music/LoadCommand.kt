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

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONTokener
import java.util.*

class LoadCommand : MusicCommand() {

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!channelChecks(event))
            return

        val attachments = event.message.attachments

        if (attachments.size == 0) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "No attachment given")
            return
        }

        if (attachments.size > 1) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "Please only attach one file at a time")
            return
        }

        val attachment = attachments[0]

        attachment.withInputStream {
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                val array = JSONArray(JSONTokener(it))

                array.filter(Objects::nonNull)
                        .forEach { obj ->
                            // This probably announces it to the channel
                            AudioUtils.ins.loadAndPlay(getMusicManager(event.guild),
                                    event.channel,
                                    event.author,
                                    obj.toString(),
                                    false,
                                    ctx.commandManager,
                                    false)
                        }

                MessageUtils.sendEmbed(event, EmbedUtils.embedField(AudioUtils.ins.embedTitle,
                        "Added ${array.length()} requested tracks."))
            } catch (exception: JSONException) {
                MessageUtils.sendError(event.message)
                MessageUtils.sendMsg(event, "Invalid JSON file!")
            }
        }
    }

    override fun getName() = "load"

    override fun help() = "Loads a given playlist file"
}