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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.MessageUtils.sendError
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class PPlayCommand : MusicCommand() {
    init {
        this.mayAutoJoin = true
        this.name = "pplay"
        this.help = "Adds a playlist to the queue"
        this.usage = "<playlist url>"
    }

    override fun run(ctx: CommandContext) {
        sendMsg(ctx, "Hey, this command will be going away soon. Please use the `/play` command instead.")

        if (ctx.args.isEmpty()) {
            sendMsg(ctx, "To few arguments, use `${ctx.prefix}$name <media link>`")
            return
        }

        val toPlay = ctx.argsRaw

        if (toPlay.length > 1024) {
            sendError(ctx.message)
            sendMsg(ctx, "Input cannot be longer than 1024 characters.")
            return
        }

        sendMsg(
            ctx,
            "Loading playlist.......\n" +
                "This may take a while depending on the size."
        )

        ctx.audioUtils.loadAndPlay(ctx.audioData, toPlay, true)
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("This command will be going away soon. Please use the `/play` command instead.").queue()
    }
}
