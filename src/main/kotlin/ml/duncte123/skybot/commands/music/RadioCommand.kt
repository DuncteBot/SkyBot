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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.entities.RadioStream
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
public class RadioCommand : MusicCommand() {

    private val radioStreams: List<RadioStream> = ArrayList()

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        if (!channelChecks(event))
            return

        val guild = event.guild
        val mng = getMusicManager(guild)
        val player = mng.player
        val scheduler = mng.scheduler

        when {
            args.isEmpty() -> {
                sendEmbed(event, EmbedUtils.defaultEmbed().build())
            }
            args.size == 1 -> {
                val radio = radioStreams.firstOrNull { it.name == args[0] }
                if (radio == null) {
                    sendMsg(event, "The stream is invalid!")
                    sendError(event.message)
                    return
                }
                au.loadAndPlay(mng, event.channel, radio.url, false)

            }
            else -> {
                sendMsg(event, "The stream name is too long! Type `$PREFIX$name` for more!")
                sendError(event.message)
            }
        }
    }

    override fun help(): String = "Adds a radio http stream"

    override fun getName(): String = "radio"

    override fun getAliases(): Array<String> = arrayOf("pstream", "stream")

    init {
        radioStreams + RadioStream("iloveradio","http://www.iloveradio.de/iloveradio.m3u","http://www.iloveradio.de/streams/")
        radioStreams + RadioStream("slam","http://19993.live.streamtheworld.com/SLAM_MP3_SC?","https://live.slam.nl/slam-live/")
    }
}

