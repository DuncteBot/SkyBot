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

import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.extensions.isNSFW
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.CommandUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

open class PlayCommand(private val skipParsing: Boolean = false) : MusicCommand() {
    private val acceptedExtensions = listOf("wav", "mkv", "mp4", "flac", "ogg", "mp3", "aac", "ts")

    init {
        this.mayAutoJoin = true
        this.name = "play"
        this.aliases = arrayOf("p")
        this.help = "Plays a song on the bot or adds it to the queue"
        this.usage = "[url/search term]"
    }

    override fun run(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            if (ctx.message.attachments.isNotEmpty() && playUploadedFile(ctx)) {
                return
            }

            val mng = ctx.audioUtils.getMusicManager(ctx.guild)
            val player = mng.player
            val scheduler = mng.scheduler

            when {
                player.isPaused -> {
                    player.isPaused = false

                    sendMsg(ctx, "Playback has been resumed.")
                }

                player.playingTrack != null -> sendMsg(ctx, "Player is already playing!")

                scheduler.queue.isEmpty() -> sendMsg(
                    ctx,
                    "The current audio queue is empty! Add something to the queue first!\n" +
                        "For example `${ctx.prefix}play https://www.youtube.com/watch?v=KKOBXrRzZwA`"
                )
            }

            return
        }

        var toPlay = ctx.argsRaw

        if (toPlay.contains(PornHubAudioSourceManager.DOMAIN_REGEX.toRegex()) && !ctx.isChannelNSFW) {
            sendMsg(ctx, "Because of thumbnails being loaded you can only use PornHub links in channels that are marked as NSFW")
            return
        }

        if (skipParsing) {
            handlePlay(toPlay, ctx)
            return
        }

        if (!AirUtils.isURL(toPlay) && !toPlay.startsWith("OCR", true)) {
            val vidId = searchYt(toPlay, ctx.variables)

            if (vidId == null) {
                MessageUtils.sendError(ctx.message)
                sendMsg(ctx, "No tracks were found")
                return
            }
            toPlay = "https://www.youtube.com/watch?v=$vidId"
        }

        handlePlay(toPlay, ctx)
    }

    private fun playUploadedFile(ctx: CommandContext): Boolean {
        val file = ctx.message.attachments
            .firstOrNull { it.fileExtension?.lowercase() in acceptedExtensions }

        if (file == null) {
            sendMsg(ctx, "Cannot play that file, please attach an audio file instead")
            return false
        }

        // returning true here to prevent going to the pause toggle
        if (!CommandUtils.isUserOrGuildPatron(ctx, false)) {
            sendMsg(ctx, "Sorry but this feature is only available to patrons")
            return true
        }

        handlePlay(file.url, ctx)

        return true
    }

    private fun searchYt(search: String, variables: Variables): String? {
        val playlist = variables.audioUtils.searchYoutube(search)

        if (playlist == null || playlist.tracks.isEmpty()) {
            return null
        }

        return playlist.tracks[0].identifier
    }

    private fun handlePlay(toPlay: String, ctx: CommandContext) {
        if (toPlay.length > 1024) {
            MessageUtils.sendError(ctx.message)
            sendMsg(ctx, "Input cannot be longer than 1024 characters.")
            return
        }

        ctx.audioUtils.loadAndPlay(ctx, toPlay, true)
    }

    private fun handlePlay(toPlay: String, variables: Variables, event: SlashCommandInteractionEvent) {
        if (toPlay.length > 1024) {
            event.reply("Input cannot be longer than 1024 characters.").queue()
            return
        }

        event.deferReply().queue()

        // TODO: get rid of CTX
        // variables.audioUtils.loadAndPlay(ctx, toPlay, true)
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOption(
                OptionType.STRING,
                "item",
                "A url or a search term to play.",
                true
            )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        var toPlay = event.getOption("item")!!.asString

        if (toPlay.contains(PornHubAudioSourceManager.DOMAIN_REGEX.toRegex()) && !event.channel.isNSFW) {
            event.reply("Because of thumbnails being loaded you can only use PornHub links in channels that are marked as NSFW").queue()
            return
        }

        if (skipParsing) {
            handlePlay(toPlay, variables, event) // TODO
            return
        }

        if (!AirUtils.isURL(toPlay) && !toPlay.startsWith("OCR", true)) {
            val vidId = searchYt(toPlay, variables)

            if (vidId == null) {
                event.reply("No tracks were found").queue()
                return
            }

            toPlay = "https://www.youtube.com/watch?v=$vidId"
        }

        handlePlay(toPlay, variables, event)
    }
}
