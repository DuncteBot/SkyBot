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

package ml.duncte123.skybot

import com.google.api.services.youtube.model.SearchResult
import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReactionHandler : EventListener {
    private val requirementsCache = arrayListOf<ReactionCacheElement>()
    private val consumerCache = hashMapOf<Long, Pair<CommandContext, List<SearchResult>>>()
    private val executor = Executors.newScheduledThreadPool(2) { r ->
        val t = Thread(r, "ReactionAwaiter")
        t.isDaemon = true
        return@newScheduledThreadPool t
    }

    private fun TextChannel.editMsg(id: Long, msg: String) = this.editMessageById(id, msg).override(true).queue(null, {})

    private fun handleUserInput(ctx: CommandContext, resSet: List<SearchResult>) {
        if (!ctx.reactionEventIsSet() && !ctx.replyIsSet()) {
            sendErrorWithMessage(ctx.message, "Internal error!")
            return
        }

        val cacheElement = requirementsCache.firstOrNull { ctx.sendId == it.authorId }

        if (cacheElement == null) {
            sendMsg(ctx, "Internal error!")
            return
        }

        if (cacheElement.equals(ctx.reactionEvent)) {
            val event = ctx.reactionEvent
            val channel = ctx.channel
            val content = event.message.contentRaw.toLowerCase()
            val index = AirUtils.parseIntSafe(content)
            val msgId = cacheElement.msgID

            if (content == "cancel") {
                channel.editMsg(msgId, "\uD83D\uDD0E Search canceled")
                requirementsCache.remove(cacheElement)
                return
            }

            if (index < 1 || index > resSet.size) {
                channel.editMsg(msgId, "\uD83D\uDD0E Invalid index")
                return
            }

            val res = resSet.getOrNull(index - 1)

            if (res == null) {
                channel.editMsg(msgId, "\uD83D\uDD0E Invalid index")
                return
            }

            ctx.audioUtils.loadAndPlay(ctx, "https://www.youtube.com/watch?v=${res.id.videoId}", true)
            requirementsCache.remove(cacheElement)

            channel.deleteMessageById(msgId).queue(null, {}) // Ignore the error if the message has already been deleted
        }
    }

    fun waitForReaction(timeoutInMillis: Long, msg: Message, userId: Long, ctx: CommandContext, resultSet: List<SearchResult>) {
        val cacheElement = ReactionCacheElement(msg.idLong, userId)
        val pair = userId to (ctx.applySentId(userId) to resultSet)

        requirementsCache.add(cacheElement)
        consumerCache[pair.first] = pair.second

        executor.schedule(
            {
                if (requirementsCache.contains(cacheElement)) {
                    requirementsCache.remove(cacheElement)
                    consumerCache.remove(userId)
                    ctx.channel.editMsg(msg.idLong, "\uD83D\uDD0E Search timed out")
                }
            },
            timeoutInMillis, TimeUnit.MILLISECONDS
        )
    }

    override fun onEvent(event: GenericEvent) {
        if (event !is GuildMessageReceivedEvent) {
            return
        }

        val checkId = event.author.idLong
        val content = event.message.contentRaw
        val intCheck = AirUtils.isInt(content) || content.toLowerCase() == "cancel"

        if (!consumerCache.containsKey(checkId) && !intCheck) {
            return
        }

        val pair = consumerCache[checkId] ?: return
        val ctx = pair.first.applyReactionEvent(event)

        if (ctx.author.idLong == event.author.idLong) {
            handleUserInput(ctx, pair.second)
            consumerCache.remove(checkId)
        }
    }
}
