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

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

@Author(nickname = "Sanduhr32", author = "Maurice R S")
open class PlayCommand : MusicCommand() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if (!channelChecks(event))
            return

        val guild = event.guild
        val mng = getMusicManager(guild)
        val player = mng.player
        val scheduler = mng.scheduler

        if (args.isEmpty()) {
            when {
                player.isPaused -> {
                    player.isPaused = false
                    MessageUtils.sendMsg(event, "Playback has been resumed.")
                }
                player.playingTrack != null -> MessageUtils.sendMsg(event, "Player is already playing!")
                scheduler.queue.isEmpty() -> MessageUtils.sendMsg(event, "The current audio queue is empty! Add something to the queue first!\n" +
                        "For example `${PREFIX}play https://www.youtube.com/watch?v=KKOBXrRzZwA`")
            }
        } else {
            var toPlay = StringUtils.join(args, " ")
            if (!AirUtils.isURL(toPlay)) {
                toPlay = "ytsearch:" + toPlay
            }

            if(toPlay.length > 1024) {
                MessageUtils.sendError(event.message)
                MessageUtils.sendMsg(event, "Input cannot be longer than 1024 characters.")
                return
            }

            audioUtils.loadAndPlay(mng, event.channel, toPlay, false)
        }
    }

    override fun help(): String = """Make the bot play song.
            |Usage: `$PREFIX$name [url/search term]`""".trimMargin()

    override fun getName(): String = "play"
}