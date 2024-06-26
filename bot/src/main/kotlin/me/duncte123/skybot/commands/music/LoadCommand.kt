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

package me.duncte123.skybot.commands.music

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.AudioData
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*

class LoadCommand : MusicCommand() {
    init {
        this.mayAutoJoin = true
        this.name = "load"
        this.help = "Loads the given playlist file\nThe playlist can be exported with `{prefix}save`"
    }

    override fun run(ctx: CommandContext) {
        val mapper = ctx.variables.jackson
        val attachments = ctx.message.attachments

        if (attachments.size == 0) {
            sendError(ctx.message)
            sendMsg(ctx, "No attachment given")
            return
        }

        if (attachments.size > 1) {
            sendError(ctx.message)
            sendMsg(ctx, "Please only attach one file at a time")
            return
        }

        val attachment = attachments[0]

        attachment.proxy.download().thenAcceptAsync {
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                val node = mapper.readTree(it)

                if (!node.isArray) {
                    sendMsg(ctx, "Provided file is not a valid JSON array")

                    return@thenAcceptAsync
                }

                val array = node as ArrayNode
                var shouldAnnounce = true

                sendMsg(ctx, "Loading ${array.size()} tracks, please wait...")

                array.filter(Objects::nonNull)
                    .forEach { obj ->
                        // This probably announces it to the channel
                        ctx.audioUtils.loadAndPlay(
                            ctx.audioData,
                            (obj as JsonNode).asText(),
                            shouldAnnounce
                        ).get()

                        shouldAnnounce = false
                    }

                sendEmbed(
                    ctx,
                    EmbedUtils.embedMessage("Added ${array.size()} requested tracks.")
                )
            } catch (exception: Exception) {
                sendError(ctx.message)
                sendMsg(ctx, "Invalid JSON file!")
            } finally {
                it.close()
            }
        }
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOption(
                OptionType.ATTACHMENT,
                "file",
                "The file created by running the save command.",
                true
            )
    }

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        val attachment = event.getOption("file")!!.asAttachment

        attachment.proxy.download().thenAcceptAsync {
            try {
                // We have to do it this way because
                // JSONArray doesn't accept a raw InputStream
                val node = variables.jackson.readTree(it)

                if (!node.isArray) {
                    event.reply("Provided file is not a valid JSON array").queue()

                    return@thenAcceptAsync
                }

                val array = node as ArrayNode
                var shouldAnnounce = true

                event.reply("Loading ${array.size()} tracks, please wait...").queue()

                array.filter(Objects::nonNull)
                    .forEach { obj ->
                        // This probably announces it to the channel
                        variables.audioUtils.loadAndPlay(
                            AudioData.fromSlash(event, variables),
                            (obj as JsonNode).asText(),
                            shouldAnnounce
                        ).get()

                        shouldAnnounce = false
                    }

                event.hook.sendMessage("Added ${array.size()} requested tracks.").queue()
            } catch (exception: Exception) {
                event.reply("Invalid JSON file!").queue()
            } finally {
                it.close()
            }
        }
    }
}
