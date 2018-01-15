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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")
@file:Suppress("MemberVisibilityCanPrivate")

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.DocumentationNeeded
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.entities.RadioStream
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
@SinceSkybot("3.52.2")
@DocumentationNeeded
public class RadioCommand : MusicCommand() {

    var radioStreams: List<RadioStream> = ArrayList()

    init {
        //Sorting via locales https://lh.2xlibre.net/locales/

        //de_DE radio stations
        radioStreams += RadioStream("iloveradio","http://www.iloveradio.de/iloveradio.m3u","http://www.iloveradio.de/streams/")
        radioStreams += RadioStream("ilove2dance","http://www.iloveradio.de/ilove2dance.m3u","http://www.iloveradio.de/streams/")
        radioStreams += RadioStream("ilovetop100charts","http://www.iloveradio.de/ilovetop100charts.m3u","http://www.iloveradio.de/streams/")
        radioStreams += RadioStream("ilovethebattle","http://www.iloveradio.de/ilovethebattle.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovedreist","http://www.iloveradio.de/ilovedreist.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovehiphop","http://www.iloveradio.de/ilovehiphopturnup.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovemashup","http://www.iloveradio.de/ilovemashup.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovebass","http://www.iloveradio.de/ilovebass.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovehistory","http://www.iloveradio.de/ilovehitshistory.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovepopstars","http://www.iloveradio.de/ilovepopstars.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("iloveandchill","http://www.iloveradio.de/iloveandchill.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("iloveberlin","http://www.iloveradio.de/iloveaboutberlin.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovexmas","http://www.iloveradio.de/ilovexmas.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovetop100pop","http://www.iloveradio.de/ilovetop100pop.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovetop100hiphop","http://www.iloveradio.de/ilovetop100hiphop.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovetop100dance&dj","http://www.iloveradio.de/ilovetop100dancedjs.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("iloveurban","http://www.iloveradio.de/ilovebigfmurbanclubbeats.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovegroovenight","http://www.iloveradio.de/ilovebigfmgroovenight.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovenitroxedm","http://www.iloveradio.de/ilovebigfmnitroxedm.m3u","http://www.iloveradio.de/streams/", false)
        radioStreams += RadioStream("ilovenitroxdeep","http://www.iloveradio.de/ilovebigfmnitroxdeep.m3u","http://www.iloveradio.de/streams/", false)

        //nl_NL radio stations
        radioStreams += RadioStream("slam","http://playerservices.streamtheworld.com/api/livestream-redirect/SLAM_MP3_SC","https://live.slam.nl/slam-live/ ")
        radioStreams += RadioStream("radio538","http://playerservices.streamtheworld.com/api/livestream-redirect/RADIO538.mp3","https://www.538.nl/", false)

        //International radio stations
        //TODO: add international radio stations
        radioStreams += RadioStream("trapfm", "http://stream.trap.fm:6004/;stream.mp3", "http://trap.fm/")
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return

        val guild = event.guild
        val mng = getMusicManager(guild)
        val scheduler = mng.scheduler

        when (args.size) {
            0 -> {
                sendMsg(event, "Insufficient args, usage: `$PREFIX$name <(full)list/station name>`")
            }
            1 -> {
                when (args[0]) {
                    "list" -> {
                        sendRadioSender(event = event)
                        return@executeCommand
                    }
                    "fulllist" -> {
                        sendRadioSender(event = event, full = true)
                        return@executeCommand
                    }
                    else -> {
                        val radio = radioStreams.firstOrNull { it.name == args[0].replace(oldValue = "❤", newValue = "love") }
                        if (radio == null) {
                            sendMsg(event, "The stream is invalid!")
                            sendError(event.message)
                            return@executeCommand
                        }
                        au.loadAndPlay(mng, event.channel, radio.url, false)
                        scheduler.queue.forEach {
                            if (it.info.uri != radio.url)
                                scheduler.nextTrack()
                        }
                    }
                }
            }
            else -> {
                sendMsg(event, "The stream name is too long! Type `$PREFIX$name (full)list` for a list of available streams!")
                sendError(event.message)
            }
        }
    }

    override fun help(): String = """Adds a radio http stream to your queue and goes to it!
            |Yes it skips all songs until it finds the stream it may bug if the current stream has the same url.
            |Usage: `$PREFIX$name <(full)list/station name>`""".trimMargin()

    override fun getName(): String = "radio"

    override fun getAliases(): Array<String> = arrayOf("pstream", "stream", "webstream", "webradio")

    private fun sendRadioSender(event: GuildMessageReceivedEvent, full: Boolean = false) {
        val streams = radioStreams
        val string = streams.filter { if(!full) it.public else true }
                .joinToString(separator = "\n") { "[${it.name}](${it.url}) ${if (it.hasWebsite()) "from [${it.website}](${it.website})" else ""}" }
        MessageBuilder().append(string).buildAll(MessageBuilder.SplitPolicy.NEWLINE).forEach {
            sendEmbed(event, EmbedUtils.defaultEmbed().setDescription(it.contentRaw).build())
        }
    }
}
