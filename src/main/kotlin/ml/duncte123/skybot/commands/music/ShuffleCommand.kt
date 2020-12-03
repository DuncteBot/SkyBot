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

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ShuffleCommand : MusicCommand() {

    init {
        this.name = "shuffle"
        this.help = "Shuffles the current queue"
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val scheduler = ctx.audioUtils.getMusicManager(event.guild).scheduler

        if (scheduler.queue.isEmpty()) {
            MessageUtils.sendMsg(ctx, "There are no songs to shuffle")
            return
        }

        scheduler.shuffle()

        MessageUtils.sendMsg(ctx, "The queue has been shuffled!")
    }
}
