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

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class PPlayCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (prejoinChecks(event)) {
            ctx.commandManager.getCommand("join")?.executeCommand(ctx)
        } else if (!channelChecks(event, ctx.audioUtils)) {
            return
        }

        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)

        if (ctx.args.isEmpty()) {
            MessageUtils.sendMsg(event, "To few arguments, use `${Settings.PREFIX}$name <media link>`")
            return
        }

        var toPlay = ctx.argsRaw
        if (!AirUtils.isURL(toPlay)) {
            toPlay = "ytsearch:$toPlay"
        }
        if (toPlay.length > 1024) {
            MessageUtils.sendError(event.message)
            MessageUtils.sendMsg(event, "Input cannot be longer than 1024 characters.")
            return
        }

        MessageUtils.sendMsg(event, "Loading playlist.......\n" +
            "This may take a while depending on the size.")
        ctx.audioUtils.loadAndPlay(mng, toPlay, ctx, true)
    }

    override fun help(): String = "Add a playlist to the queue."

    override fun getName(): String = "pplay"
}
