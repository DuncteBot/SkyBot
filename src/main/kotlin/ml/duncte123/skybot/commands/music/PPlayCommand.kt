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

import me.duncte123.botcommons.messaging.MessageUtils.sendError
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class PPlayCommand : MusicCommand() {

    init {
        this.withAutoJoin = true
        this.name = "pplay"
        this.helpFunction = {_,_ -> "Adds a playlist to the queue"}
        this.usageInstructions = {invoke, prefix -> "`$prefix$invoke <playlist url>`"}
    }

    override fun run(ctx: CommandContext) {

        val event = ctx.event
        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)

        if (ctx.args.isEmpty()) {
            sendMsg(event, "To few arguments, use `${ctx.prefix}$name <media link>`")
            return
        }

        val toPlay = ctx.argsRaw

        if (toPlay.length > 1024) {
            sendError(event.message)
            sendMsg(event, "Input cannot be longer than 1024 characters.")
            return
        }

        sendMsg(event, "Loading playlist.......\n" +
            "This may take a while depending on the size.")

        ctx.audioUtils.loadAndPlay(mng, toPlay, ctx)
    }
}
