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
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import ml.duncte123.skybot.utils.YoutubeUtils
import java.util.concurrent.TimeUnit

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class SearchCommand : MusicCommand() {

    init {
        this.withAutoJoin = true
        this.name = "search"
        this.help = "Search for a song to play"
        this.usage = "<search term>"
    }

    override fun run(ctx: CommandContext) {

        val event = ctx.event

        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val handler = ctx.reactionHandler
        val isPatron = isUserOrGuildPatron(event, false)

        val timeout = when {
            isDev(event.author) || isPatron -> 60L
            else -> 15L
        }

        val searchLimit = if (isPatron) 15L else 5L

        val toPlay = ctx.argsRaw
        val res = YoutubeUtils.searchYoutube(toPlay, ctx.config.apis.googl, searchLimit)

        if (res.isEmpty()) {
            sendMsg(event, "\uD83D\uDD0E No results found.")
            return
        }

        val string = buildString {
            res.map { it.snippet.title }.forEachIndexed { index: Int, s: String ->
                append(index + 1).append(". ").append(s).append("\n")
            }

            append("\n\n")
            append("Type the number of the song that you want to play or type `cancel` to cancel your search")
        }

        sendEmbed(ctx.channel, EmbedUtils.embedMessage(string)) {
            handler.waitForReaction(TimeUnit.SECONDS.toMillis(timeout), it, event.author.idLong, ctx, res)
        }

    }
}
