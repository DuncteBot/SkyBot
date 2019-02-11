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

package ml.duncte123.skybot

import com.google.api.services.youtube.model.SearchResult
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ReactionHandler : ListenerAdapter() {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2) { r -> Thread(r, "ReactionAwaiter") }
    private var requirementsCache: List<ReactionCacheElement> = ArrayList()
    private var consumerCache: Map<Long, Pair<CommandContext, List<SearchResult>>> = HashMap()

    private val defaultConsumer: BiConsumer<CommandContext, List<SearchResult>> = BiConsumer { ctx, resSet ->

        if (!ctx.reactionEventIsSet() && !ctx.replyIsSet()) {
            MessageUtils.sendErrorWithMessage(ctx.message, "Internal error!")
            return@BiConsumer
        }

        val cacheElement = requirementsCache.firstOrNull { ctx.sendId == it.authorId }

        if (cacheElement == null) {
            MessageUtils.sendMsg(ctx.event, "Internal error!")
            return@BiConsumer
        }

        if (cacheElement.equals(ctx.reactionEvent)) {
            val event = ctx.reactionEvent
            val content = event.message.contentRaw.toLowerCase()
            val index = AirUtils.parseIntSafe(content)
            val msgId = cacheElement.msgID

            if (content == "cancel") {
                ctx.channel.editMessageById(msgId, "\uD83D\uDD0E Search canceled").override(true).queue()
                requirementsCache = requirementsCache - cacheElement

                return@BiConsumer
            }

            if (index == -1 || index > resSet.size) {
                ctx.channel.editMessageById(msgId, "\uD83D\uDD0E Invalid index").override(true).queue()

                return@BiConsumer
            }

            val res = resSet.getOrNull(index - 1)
            if (res == null) {
                ctx.channel.editMessageById(msgId, "\uD83D\uDD0E Invalid index").override(true).queue()

                return@BiConsumer
            }

            ctx.audioUtils.loadAndPlay(ctx.audioUtils.getMusicManager(ctx.guild),
                "https://www.youtube.com/watch?v=${res.id.videoId}", ctx, false)
            requirementsCache = requirementsCache - cacheElement

            ctx.channel.deleteMessageById(msgId).queue(null) {} // Ignore the error if the message has already been deleted
        }

    }

    fun waitForReaction(timeoutInMillis: Long, msg: Message, userId: Long, context: CommandContext, resultSet: List<SearchResult>) {
        val cacheElement = ReactionCacheElement(msg.idLong, userId)
        val pair = userId to (context.applySentId(userId) to resultSet)

        requirementsCache = requirementsCache + cacheElement
        consumerCache = consumerCache + pair

        executor.schedule({
            if (requirementsCache.contains(cacheElement)) {
                requirementsCache = requirementsCache - cacheElement
                consumerCache = consumerCache - userId

                context.channel.editMessageById(msg.idLong, "\uD83D\uDD0E Search timed out").override(true).queue()
            }
        }, timeoutInMillis, TimeUnit.MILLISECONDS)
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        val checkId = event.author.idLong
        val intCheck = AirUtils.isInt(event.message.contentRaw) || event.message.contentRaw.toLowerCase() == "cancel"

        if (!consumerCache.containsKey(checkId) && !intCheck) {
            return
        }

        val pair = consumerCache[checkId] ?: return

        val ctx = pair.first.applyReactionEvent(event)

        if (ctx.author.idLong == event.author.idLong) {
            defaultConsumer.accept(ctx, pair.second)
            consumerCache = consumerCache - checkId
        }
    }
}
