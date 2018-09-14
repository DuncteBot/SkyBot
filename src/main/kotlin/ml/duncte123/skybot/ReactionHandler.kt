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

package ml.duncte123.skybot

import com.google.api.services.youtube.model.SearchResult
import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

class ReactionHandler : ListenerAdapter() {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2) { r -> Thread(r, "ReactionAwaiter") }
    private val reactions: List<String> = listOf("1\u20E3", "2\u20E3", "3\u20E3", "4\u20E3", "5\u20E3",
                                                 "6\u20E3", "7\u20E3", "8\u20E3", "9\u20E3", "\uD83D\uDD1F")
    private var requirementsCache: List<ReactionCacheElement> = ArrayList()
    private var consumerCache: Map<Long, Pair<CommandContext, List<SearchResult>>> = HashMap()

    private val defaultConsumer: BiConsumer<CommandContext, List<SearchResult>> = BiConsumer { ctx, resSet ->
        if (!ctx.reactionEventIsSet() && !ctx.replyIsSet()) {
            MessageUtils.sendErrorWithMessage(ctx.message, "Internal error!")
            return@BiConsumer
        }
        val cacheElement = requirementsCache.firstOrNull { ctx.replyId == it.msgID }

        if (cacheElement == null) {
            MessageUtils.sendMsg(ctx.event, "Internal error!")
            return@BiConsumer
        }

        if (cacheElement.equals(ctx.reactionEvent)) {
            val index = parseReaction(ctx.reactionEvent.reactionEmote.name)
            if (index == -1) {
                ctx.channel.editMessageById(ctx.replyId, "\uD83D\uDD0E Search canceled").override(true  ).queue {
                it.clearReactions().queueAfter(15, TimeUnit.SECONDS)
            }
                return@BiConsumer
            }
            ctx.audioUtils.loadAndPlay(ctx.audioUtils.getMusicManager(ctx.guild), ctx.channel, ctx.author,
                    "https://www.youtube.com/watch?v=${resSet[index].id.videoId}", ctx, false)
            requirementsCache -= cacheElement
            ctx.channel.deleteMessageById(ctx.replyId).queue()
        }

    }

    public fun waitForReaction(timeoutInMillis: Long, msg: Message, userId: Long, context: CommandContext, resultSet: List<SearchResult>) {
        val msgId = msg.idLong
        val reacs = reactions.subList(0, resultSet.size).plus("\u274C")
        for (s in reacs)
            msg.addReaction(s).queue()
        val cacheElement = ReactionCacheElement(msgId, userId, reacs)
        val pair = msgId to (context.applySentId(msgId) to resultSet)
        requirementsCache += cacheElement
        consumerCache += pair

        executor.schedule({
            requirementsCache -= cacheElement
            consumerCache -= msgId
            context.channel.editMessageById(msgId, "\uD83D\uDD0E Search canceled").override(true).queue {
                it.clearReactions().queueAfter(15, TimeUnit.SECONDS)
            }
        }, timeoutInMillis, TimeUnit.MILLISECONDS)
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        val msgId = event.messageIdLong
        if (!consumerCache.containsKey(msgId) && !reactions.plus("\u274C").contains(event.reactionEmote.name))
            return
        val pair = consumerCache[msgId] ?: return
        val ctx = pair.first.applyReactionEvent(event)

        event.channel.getMessageById(msgId).queue { msg ->
            if (ctx.author.idLong == event.user.idLong) {
                defaultConsumer.accept(ctx, pair.second)
                consumerCache -= msgId
            }
        }
    }

    private fun parseReaction(reaction: String): Int {
        val retVal = when (reaction) {
            "1\u20E3" -> 1
            "2\u20E3" -> 2
            "3\u20E3" -> 3
            "4\u20E3" -> 4
            "5\u20E3" -> 5
            "6\u20E3" -> 6
            "7\u20E3" -> 7
            "8\u20E3" -> 8
            "9\u20E3" -> 9
            "\uD83D\uDD1F" -> 10
            else -> 0
        }
        return (retVal -1)
    }
}
