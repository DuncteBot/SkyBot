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

import me.duncte123.botcommons.messaging.MessageUtils.*
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ReactionHandler : EventListener {
    private val requirementsCache = arrayListOf<ReactionCacheElement>()
    private val consumerCache = hashMapOf<Long, CommandContext>()
    private val executor = Executors.newScheduledThreadPool(2) { r ->
        val t = Thread(r, "ReactionAwaiter")
        t.isDaemon = true
        return@newScheduledThreadPool t
    }

    private fun TextChannel.editMsg(id: Long, msg: String) = this.editMessageById(id, msg)
        .override(true)
        .queue()

    private fun CommandContext.editMsg(msg: String) = this.buttonEvent.hook.editOriginal(msg)
        .setEmbeds(listOf())
        .setActionRows(listOf())
        .queue()

    private fun handleUserInput(ctx: CommandContext) {
        if (!ctx.buttonEventIsSet() && !ctx.replyIsSet()) {
            // TODO: keep?
            sendErrorWithMessage(ctx.message, "Internal error!")
            return
        }

        val cacheElement = requirementsCache.firstOrNull { ctx.sendId == it.authorId }

        if (cacheElement == null) {
            // TODO: keep?
            sendMsg(ctx, "Internal error!")
            return
        }

        if (cacheElement.equals(ctx.buttonEvent)) {
            val event = ctx.buttonEvent
            val channel = ctx.channel
            val buttonId = event.componentId

            if (buttonId.startsWith("cancel-search")) {
                ctx.editMsg("\uD83D\uDD0E Search canceled")
                requirementsCache.remove(cacheElement)
                return
            }

            val items = buttonId.split(":")

            if (items.size != 3 || items[0] != "select-track") {
                ctx.editMsg("\uD83D\uDD0E Invalid button")
                return
            }

            ctx.audioUtils.loadAndPlay(ctx, "https://www.youtube.com/watch?v=${items[1]}", true)
            requirementsCache.remove(cacheElement)

            channel.deleteMessageById(cacheElement.msgID)
                .queue(null, ignore(UNKNOWN_MESSAGE)) // Ignore the error if the message has already been deleted
        }
    }

    fun waitForReaction(timeoutInMillis: Long, msg: Message, userId: Long, ctx: CommandContext) {
        val cacheElement = ReactionCacheElement(msg.idLong, userId)

        requirementsCache.add(cacheElement)
        consumerCache[userId] = ctx.applySentId(userId)

        executor.schedule({
            try {
                if (requirementsCache.contains(cacheElement)) {
                    requirementsCache.remove(cacheElement)
                    consumerCache.remove(userId)
                    ctx.channel.editMsg(msg.idLong, "\uD83D\uDD0E Search timed out")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, timeoutInMillis, TimeUnit.MILLISECONDS)
    }

    override fun onEvent(event: GenericEvent) {
        if (event !is ButtonClickEvent) {
            return
        }

        if (!event.componentId.endsWith(event.user.id)) {
            event.deferReply(true)
                .setContent("This button was not meant for you.")
                .queue()
            return
        }

        val checkId = event.user.idLong

        if (!consumerCache.containsKey(checkId)) {
            event.deferEdit()
                .setContent("Event was not registered, auto remove in 10 seconds.")
                .setEmbeds(listOf())
                .setActionRows(listOf())
                .queue {
                    it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
                }
            return
        }

        val fromCache = consumerCache[checkId] ?: return
        val ctx = fromCache.applyButtonEvent(event)

        if (ctx.author.idLong == checkId) {
            // ack discord to let them know we're good
            event.deferEdit().queue()
            handleUserInput(ctx)
            consumerCache.remove(checkId)
        }
    }
}
