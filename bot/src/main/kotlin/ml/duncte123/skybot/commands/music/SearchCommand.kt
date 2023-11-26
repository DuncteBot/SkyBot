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
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.Emotes.SEARCH_EMOTE
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class SearchCommand : MusicCommand() {

    init {
        this.mayAutoJoin = true
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

        val searchLimit = if (isPatron) 20 else 5

        val toPlay = ctx.argsRaw
        val res = ctx.audioUtils.searchYoutube(ctx.guildId, toPlay)

        if (res.isNullOrEmpty()) {
            sendMsg(ctx, "$SEARCH_EMOTE No results found.")
            return
        }

        val tracks = res.take(searchLimit)

        val string = buildString {
            tracks.map { it.info.title }.forEachIndexed { index: Int, s: String ->
                append(index + 1).append(". ").append(s).append("\n")
            }

            append("\n\n")
            append("Click the button with the number of the song that you want to play, or click `cancel` to cancel your search")
            if (!isPatron) {
                append("\n(hint: patron supporters get access to more search results to pick from)")
            }
        }

        val componentId = "search-menu:${UUID.randomUUID()}:$userId"
        val menu = StringSelectMenu.create(componentId)
            .setPlaceholder("Select a song to play")

        tracks.forEachIndexed { index, track ->
            val title = track.info.title

            menu.addOption(
                "${index + 1}) ${title.substring(0, min(title.length, 20)).trim()}",
                track.info.identifier,
                title.substring(0, min(title.length, 50)).trim() // TODO: full title or url?
            )
        }

        menu.addOption("Cancel", "cancel-search")

        sendMsg(
            MessageConfig.Builder()
                .setChannel(ctx.channel)
                .addEmbed(EmbedUtils.embedMessage(string))
                .configureMessageBuilder {
                    it.addActionRow(menu.build())
                }
                .setSuccessAction {
                    handler.waitForReaction(TimeUnit.SECONDS.toMillis(timeout), it, componentId, userId, ctx)
                }
                .build()
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue()
    }
}
