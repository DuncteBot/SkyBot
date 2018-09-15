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

package ml.duncte123.skybot.commands.music

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.YoutubeUtils
import java.util.concurrent.TimeUnit

class SearchCommand : MusicCommand() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (prejoinChecks(event)) {
            ctx.commandManager.getCommand("join")?.executeCommand(ctx)
        } else if (!channelChecks(event, ctx.audioUtils)) {
            return
        }

        val guild = event.guild
        val mng = getMusicManager(guild, ctx.audioUtils)
        val player = mng.player
        val scheduler = mng.scheduler

        if (ctx.args.isEmpty()) {
            when {
                player.isPaused -> {
                    player.isPaused = false
                    MessageUtils.sendMsg(event, "Playback has been resumed.")
                }
                player.playingTrack != null -> MessageUtils.sendMsg(event, "Player is already playing!")
                scheduler.queue.isEmpty() -> MessageUtils.sendMsg(event, "The current audio queue is empty! Add something to the queue first!\n" +
                        "For example `${PREFIX}play https://www.youtube.com/watch?v=KKOBXrRzZwA`")
            }
        } else {
            val handler = ctx.reactionHandler

            val timeout = when {
                isDev(event.author) -> 60L
                isUserOrGuildPatron(event, false) -> 30L
                else -> 15L
            }

            val searchLimit = if(isUserOrGuildPatron(event, false)) 10L else 5L

            val toPlay = ctx.argsRaw
            val res = YoutubeUtils.searchYoutube(toPlay, ctx.config.apis.googl, searchLimit)

            val string = buildString {
                res.map { it.snippet.title }.forEachIndexed { index: Int, s: String ->
                    append(index + 1).append(". ").append(s).append("\n")
                }
            }

            event.channel.sendMessage(EmbedUtils.defaultEmbed().appendDescription(string).build()).queue {
                handler.waitForReaction(TimeUnit.SECONDS.toMillis(timeout), it, event.author.idLong, ctx, res)
            }
        }
    }

    override fun getName(): String = "search"

    override fun help(): String = """Make the bot play song.
            |Usage: `$PREFIX$name [search term]`""".trimMargin()
}