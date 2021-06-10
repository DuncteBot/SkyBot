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
import ml.duncte123.skybot.objects.Emotes.SEARCH_EMOTE
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import ml.duncte123.skybot.utils.YoutubeUtils
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import java.util.concurrent.TimeUnit
import kotlin.math.min

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
        val userId = ctx.author.idLong

        val timeout = when {
            isDev(userId) || isPatron -> 60L
            else -> 15L
        }

        val searchLimit = if (isPatron) 15L else 5L

        val toPlay = ctx.argsRaw
        val res = YoutubeUtils.searchYoutube(toPlay, ctx.config.apis.googl, searchLimit)

        if (res.isEmpty()) {
            sendMsg(ctx, "$SEARCH_EMOTE No results found.")
            return
        }

        val string = buildString {
            res.map { it.snippet.title }.forEachIndexed { index: Int, s: String ->
                append(index + 1).append(". ").append(s).append("\n")
            }

            append("\n\n")
            append("Click the button with the number of the song that you want to play, or click `cancel` to cancel your search")
        }

        /*val rows = res.mapIndexed { index, it ->
            Button.secondary("select-track:${it.id.videoId}:$userId", "${index + 1}")
        }
            .chunked(5)
            .map { ActionRow.of(it) }
            .toMutableList()

        rows.add(ActionRow.of(Button.danger("cancel-search:$userId", "Cancel")))*/

        val menu = SelectionMenu.Builder("search-menu:$userId")
            .setPlaceholder("Select a song to play")

        res.forEachIndexed { index, searchResult ->
            val title = searchResult.snippet.title

            menu.addOption(
                "${index + 1}) ${title.substring(0, min(title.length, 20)).trim()}",
                searchResult.id.videoId,
                title.substring(0, min(title.length, 50)).trim() // TODO: full title or url?
            )
        }

        menu.addOption("Cancel", "cancel-search")

        sendMsg(
            MessageConfig.Builder()
                .setChannel(ctx.channel)
                .setEmbed(EmbedUtils.embedMessage(string))
                .configureMessageBuilder {
                    it.setActionRows(ActionRow.of(menu.build()))
                }
                .setSuccessAction {
                    handler.waitForReaction(TimeUnit.SECONDS.toMillis(timeout), it, userId, ctx)
                }
                .build()
        )
    }
}
