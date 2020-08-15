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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.RadioStream
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot("3.52.2")
class RadioCommand : MusicCommand() {
    var radioStreams = arrayListOf<RadioStream>()

    init {
        this.withAutoJoin = true
        this.name = "radio"
        this.aliases = arrayOf("pstream", "stream", "webstream", "webradio")
        this.help = "Adds a radio http stream to your queue and goes to it"
        this.usage = "<(full)list/station name>"

        loadStations()
    }

    override fun run(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            sendMsg(ctx, "Insufficient args, usage: `${ctx.prefix}$name <(full)list/station name>`")
            return
        }

        val mng = getMusicManager(ctx.jdaGuild, ctx.audioUtils)
        val scheduler = mng.scheduler

        when (ctx.args[0]) {
            "list" -> {
                sendRadioSender(ctx)
                return
            }
            "fulllist" -> {
                sendMsg(ctx, "The full list of radio streams can be found on <https://dunctebot.com/radiostreams>")
                return
            }
            else -> {
                val search = ctx.argsRaw.toLowerCase()
                val radio = radioStreams.firstOrNull {
                    it.name.toLowerCase().contains(search) || it.website.toLowerCase().contains(search)
                }

                if (radio == null) {
                    sendErrorWithMessage(ctx.message, "No stream found for \"$search\"")
                    return
                }

                mng.player.stopTrack()
                mng.player.isPaused = false
                scheduler.queue.clear()
                ctx.audioUtils.loadAndPlay(mng, radio.url, ctx)
            }
        }
    }

    private fun sendRadioSender(ctx: CommandContext) {
        val selectedStreams = arrayListOf<RadioStream>()

        repeat(5) {
            var selected = radioStreams.random()

            while (selectedStreams.contains(selected)) {
                selected = radioStreams.random()
            }

            selectedStreams.add(selected)
        }

        val string = selectedStreams.joinToString(separator = "\n", transform = RadioStream::toEmbedString)
        val embed = EmbedUtils.embedMessage(string)
            .setTitle("Here are 5 random entries from our radio list")

        sendEmbed(ctx, embed)
    }

    private fun loadStations() {
        // Fetch the streams from github
        val json = WebUtils.ins
            .getJSONArray("https://raw.githubusercontent.com/DuncteBot/dunctebot.github.io/development/resources/radio_streams.json")
            .execute()

//        val streams = this.javaClass.getResource("/radio_streams.json").readText()
//        val json = DataArray.fromJson(streams)

        // Clear before adding more (in case of reloading)
        radioStreams.clear()

        json.forEach {
            radioStreams.add(
                RadioStream(
                    name = it["name"].asText(),
                    url = it["audio"].asText(),
                    website = it["website"].asText()
                )
            )
        }
    }
}
