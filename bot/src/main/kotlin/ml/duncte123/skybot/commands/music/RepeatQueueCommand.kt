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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RepeatQueueCommand : MusicCommand() {
    init {
        this.name = "repeatqueue"
        this.aliases = arrayOf("loopqueue")
        this.help = "Turn on or off looping of the entire queue"
    }

    override fun run(ctx: CommandContext) {
        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        val scheduler = mng.scheduler

        if (scheduler.isLooping) {
            sendMsg(ctx, "Cannot turn on queue looping when track looping is enabled.")
            return
        }

        val newState = !scheduler.isLoopingQueue
        scheduler.isLoopingQueue = newState

        sendMsg(
            ctx,
            "Looping the current queue has been turned **${if (newState) "on" else "off"}**!"
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue()
    }
}
