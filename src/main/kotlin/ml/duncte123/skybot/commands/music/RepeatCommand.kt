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
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class RepeatCommand : MusicCommand() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if (!channelChecks(event))
            return

        val mng = getMusicManager(event.guild)
        val scheduler = mng.scheduler
        mng.latestChannel = event.channel

        if (args.size == 1 && args[0] == "playlist") {
            scheduler.isRepeatingPlaylists = !scheduler.isRepeatingPlaylists
        }

        scheduler.isRepeating = !scheduler.isRepeating
        MessageUtils.sendMsg(event, "Player was set to: **${if (scheduler.isRepeating) " " else "not"} repeating**")
    }

    override fun help(): String = "Makes the player repeat the currently playing song"

    override fun getName(): String = "repeat"

    override fun getAliases(): Array<String> = arrayOf("loop")
}