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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import ml.duncte123.skybot.utils.YoutubeUtils
import java.util.concurrent.TimeUnit

class SearchCommand : MusicCommand() {

    init {
        this.withAutoJoin = true
        this.name = "search"
        this.help = "Search for a song to play"
        this.usage = "<search term>"
    }

    override fun run(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val handler = ctx.reactionHandler
        val isPatron = isUserOrGuildPatron(ctx, false)
        val author = ctx.author

        val timeout = when {
            isDev(author) || isPatron -> 60L
            else -> 15L
        }

        val searchLimit = if (isPatron) 15L else 5L

        val toPlay = ctx.argsRaw
        val res = YoutubeUtils.searchYoutube(toPlay, ctx.config.apis.googl, searchLimit)

        if (res.isEmpty()) {
            sendMsg(ctx, "\uD83D\uDD0E No results found.")
            return
        }

        val string = buildString {
            res.map { it.snippet.title }.forEachIndexed { index: Int, s: String ->
                append(index + 1).append(". ").append(s).append("\n")
            }

            append("\n\n")
            append("Type the number of the song that you want to play or type `cancel` to cancel your search")
        }

        sendMsg(
            MessageConfig.Builder()
                .setChannel(ctx.channel)
                .addEmbed(EmbedUtils.embedMessage(string))
                .setSuccessAction {
                    handler.waitForReaction(TimeUnit.SECONDS.toMillis(timeout), it, author.idLong, ctx, res)
                }
                .build()
        )
    }
}
