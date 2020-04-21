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
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.RadioStream
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.utils.data.DataArray

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

        val event = ctx.event
        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)
        val scheduler = mng.scheduler

        when (ctx.args.size) {
            0 -> {
                sendMsg(event, "Insufficient args, usage: `${ctx.prefix}$name <(full)list/station name>`")
            }
            1 -> {
                when (ctx.args[0]) {
                    "list" -> {
                        sendRadioSender(event = event)
                        return
                    }
                    // TODO: send a link to the website or something, this is too huge
                    "fulllist" -> {
                        sendRadioSender(event = event, full = true)
                        return
                    }
                    else -> {
                        val search = ctx.args[0]
                        val radio = radioStreams.firstOrNull { it.name == search }

                        if (radio == null) {
                            sendErrorWithMessage(event.message, "No stream found for \"$search\"")
                            return
                        }

                        mng.player.stopTrack()
                        scheduler.queue.clear()
                        ctx.audioUtils.loadAndPlay(mng, radio.url, ctx)
                    }
                }
            }
            else -> {
                sendErrorWithMessage(event.message, "The stream name is too long! Type `${ctx.prefix}$name (full)list` for a list of available streams!")
            }
        }
    }

    private fun sendRadioSender(event: GuildMessageReceivedEvent, full: Boolean = false) {
        val selectedStreams = if (full) {
            radioStreams
        } else {
            val out = arrayListOf<RadioStream>()

            repeat(5) {
                var selected = radioStreams.random()

                while (out.contains(selected)) {
                    selected = radioStreams.random()
                }

                out.add(selected)
            }

            out
        }
        val string = selectedStreams.joinToString(separator = "\n") { it.toEmbedString() }

        MessageBuilder()
            .append(string)
            .buildAll(MessageBuilder.SplitPolicy.NEWLINE)
            .forEachIndexed { index, it ->
                val embed = EmbedUtils.embedMessage(it.contentRaw)

                if (!full && index == 0) {
                    embed.setTitle("Here are 5 random entries from our radio list")
                }

                sendEmbed(event, embed)
            }
    }

    private fun loadStations() {
        val streams = this.javaClass.getResource("/radio_streams.json").readText()
        val json = DataArray.fromJson(streams)

        json.forEach {
            it as HashMap<*, *>

            radioStreams.add(
                RadioStream(
                    name = it["name"].toString(),
                    url = it["audio"].toString(),
                    website = it["website"].toString(),
                    listed = false
                )
            )
        }
    }
}
