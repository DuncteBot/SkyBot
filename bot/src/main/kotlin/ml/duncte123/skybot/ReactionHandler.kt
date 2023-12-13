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

import me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.Emotes.SEARCH_EMOTE
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.ThreadUtils.runOnVirtual
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE
import java.util.concurrent.TimeUnit

class ReactionHandler : EventListener {
    private val requirementsCache = arrayListOf<ReactionCacheElement>()
    private val consumerCache = hashMapOf<String, CommandContext>()

    private fun MessageChannelUnion.editMsg(id: Long, msg: String) = this.asGuildMessageChannel()
        .editMessageById(id, msg)
        .setReplace(true)
        .queue()

    private fun CommandContext.editMsg(msg: String) = this.selectionEvent.channel
        .editMessageById(this.selectionEvent.messageIdLong, msg)
        .setReplace(true)
        .queue()

    private fun handleUserInput(ctx: CommandContext) {
        if (!ctx.selectionEventIsSet() && !ctx.replyIsSet()) {
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

        if (cacheElement.equals(ctx.selectionEvent)) {
            // remove from cache
            requirementsCache.remove(cacheElement)

            val event = ctx.selectionEvent
            val channel = ctx.channel
            val selected = event.values

            if (selected.isEmpty()) {
                ctx.editMsg("$SEARCH_EMOTE Search canceled (nothing selected)")
                requirementsCache.remove(cacheElement)
                return
            }

            val selectedId = selected[0]

            if (selectedId == "cancel-search") {
                ctx.editMsg("$SEARCH_EMOTE Search canceled")
                return
            }

            ctx.audioUtils.loadAndPlay(ctx.audioData, "https://www.youtube.com/watch?v=$selectedId", true)
            channel.deleteMessageById(cacheElement.msgID)
                .queue(null, ignore(UNKNOWN_MESSAGE)) // Ignore the error if the message has already been deleted
        }
    }

    fun waitForReaction(timeoutInMillis: Long, msg: Message, componentId: String, userId: Long, ctx: CommandContext) {
        val cacheElement = ReactionCacheElement(msg.idLong, userId)

        requirementsCache.add(cacheElement)
        consumerCache[componentId] = ctx.applySentId(userId)

        SkyBot.SYSTEM_POOL.schedule(
            {
                runOnVirtual {
                    try {
                        if (requirementsCache.contains(cacheElement)) {
                            requirementsCache.remove(cacheElement)
                            consumerCache.remove(componentId)
                            ctx.channel.editMsg(msg.idLong, "$SEARCH_EMOTE Search timed out")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            timeoutInMillis, TimeUnit.MILLISECONDS
        )
    }

    override fun onEvent(event: GenericEvent) {
        if (event !is StringSelectInteractionEvent) {
            return
        }

        val componentId = event.componentId

        if (!componentId.endsWith(event.user.id)) {
            event.deferReply(true)
                .setContent("This button was not meant for you.")
                .queue()
            return
        }

        if (!consumerCache.containsKey(componentId)) {
            event.deferReply(true)
                .setContent("That menu is not registered!")
                .queue()
            return
        }

        val fromCache = consumerCache[componentId] ?: return
        val ctx = fromCache.applyButtonEvent(event)

        val checkId = event.user.idLong

        if (ctx.author.idLong == checkId) {
            // ack discord to let them know we're good
            event.deferEdit().queue()
            handleUserInput(ctx)
            consumerCache.remove(componentId)
        }
    }
}
