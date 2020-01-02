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

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot("3.52.2")
class RadioCommand : MusicCommand() {

    var radioStreams: ArrayList<RadioStream> = ArrayList()

    init {
        this.withAutoJoin = true
        this.name = "radio"
        this.aliases = arrayOf("pstream", "stream", "webstream", "webradio")
        this.helpFunction = { _, _ -> "Adds a radio http stream to your queue and goes to it" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <(full)list/station name>`" }
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
        val streams = radioStreams
        val string = streams.filter { if (!full) it.public else true }
            .joinToString(separator = "\n") { it.toEmbedString() }
        for (it in MessageBuilder().append(string).buildAll(MessageBuilder.SplitPolicy.NEWLINE)) {
            sendEmbed(event, EmbedUtils.defaultEmbed().setDescription(it.contentRaw).build())
        }
    }

    private fun loadStations() {
        //Sorting via locales https://lh.2xlibre.net/locales/

        //de_DE radio stations
        radioStreams.add(RadioStream("iloveradio", "http://stream01.iloveradio.de/iloveradio1.mp3", "http://www.iloveradio.de/streams/"))
        // TODO: why so many
        // TODO: Make a json file or api endpoint with all streams for easy updating
        /*
        radioStreams.add(ILoveStream(stationName = "iloveradio", channel = 1))
        radioStreams.add(ILoveStream(stationName = "ilove2dance", channel = 2))
        radioStreams.add(ILoveStream(stationName = "ilovetop100charts", channel = 9))
        radioStreams.add(ILoveStream(stationName = "ilovethebattle", channel = 3, npChannel = 4, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovebass", channel = 4, npChannel = 3, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovemashup", channel = 5, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovedreist", channel = 6, public = false))
        radioStreams.add(ILoveStream(stationName = "iloveberlin", channel = 7, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovexmas", channel = 8, public = false))
        radioStreams.add(ILoveStream(stationName = "iloveandchill", channel = 10, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovetop100dance&dj", channel = 103, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovetop100pop", channel = 105, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovetop100hiphop", channel = 108, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovepopstars", channel = 11, npChannel = 16, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovehistory", channel = 12, npChannel = 15, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovehiphop", channel = 13, npChannel = 17, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovethesun", channel = 15, npChannel = 19))
        radioStreams.add(ILoveStream(stationName = "iloveurban", channel = -1, npChannel = 12, internal = false, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovegroove", channel = -1, npChannel = 13, internal = false, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovenitroxedm", channel = -1, npChannel = 11, internal = false, public = false))
        radioStreams.add(ILoveStream(stationName = "ilovenitroxdeep", channel = -1, npChannel = 24, internal = false, public = false))*/

        //nl_NL radio stations
        radioStreams.add(RadioStream("slam", "http://playerservices.streamtheworld.com/api/livestream-redirect/SLAM_MP3_SC", "https://live.slam.nl/slam-live/"))
        radioStreams.add(RadioStream("radio538", "http://playerservices.streamtheworld.com/api/livestream-redirect/RADIO538.mp3", "https://www.538.nl/"))
        radioStreams.add(RadioStream("3fm", "http://icecast.omroep.nl/3fm-sb-mp3", "https://www.npo3fm.nl/", false))
        radioStreams.add(RadioStream("skyradio", "http://playerservices.streamtheworld.com/api/livestream-redirect/SKYRADIO_SC", "http://www.skyradio.nl/", false))
        radioStreams.add(RadioStream("qmusic", "http://icecast-qmusicnl-cdp.triple-it.nl/Qmusic_nl_live_96.mp3", "http://qmusic.nl/", false))

        //International radio stations
        //TODO: add international radio stations
        radioStreams.add(RadioStream("trapfm", "http://stream.trap.fm:6004/;stream.mp3", "http://trap.fm/"))
        radioStreams.add(RadioStream("listen.moe", "https://listen.moe/stream", "https://listen.moe/stream"))
    }
}
