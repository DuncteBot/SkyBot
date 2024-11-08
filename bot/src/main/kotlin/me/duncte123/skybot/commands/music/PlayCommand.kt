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

import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.extensions.isNSFW
import me.duncte123.skybot.objects.AudioData
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.jvm.optionals.getOrNull

open class PlayCommand(private val skipParsing: Boolean = false) : MusicCommand() {
    private val pornhubRegex = "https?://([a-z]+\\.)?pornhub\\.(com|net|org)"
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

            val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
            val player = mng.player.getOrNull()

            if (player == null) {
                sendMsg(ctx, "Nothing is playing currently, add an argument to play something.")
            } else {
                val scheduler = mng.scheduler

                when {
                    player.paused -> {
                        player.setPaused(false).subscribe()

                        sendMsg(ctx, "Playback has been resumed.")
                    }

                    player.track != null -> sendMsg(ctx, "Player is already playing!")

                    scheduler.queue.isEmpty() -> sendMsg(
                        ctx,
                        "The current audio queue is empty! Add something to the queue first!\n" +
                            "For example `${ctx.prefix}play https://www.youtube.com/watch?v=KKOBXrRzZwA`"
                    )
                }
            }

            return
        }

        var toPlay = ctx.argsRaw

        if (toPlay.contains(pornhubRegex.toRegex()) && !ctx.isChannelNSFW) {
            sendMsg(
                ctx,
                "Because of thumbnails being loaded you can only use PornHub links in channels that are marked as NSFW"
            )
            return
        }

        if (skipParsing) {
            handlePlay(toPlay, ctx)
            return
        }

        if (!AirUtils.isURL(toPlay) && !toPlay.startsWith("OCR", true)) {
            val songUrl = searchSong(ctx.guildId, toPlay, ctx.variables)

            if (songUrl == null) {
                MessageUtils.sendError(ctx.message)
                sendMsg(ctx, "No tracks were found")
                return
            }
            toPlay = songUrl
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

        handlePlay(file.url, ctx)

        return true
    }

    private fun searchSong(guildId: Long, search: String, variables: Variables): String? {
        val playlist = variables.audioUtils.searchForSong(guildId, search)

        if (playlist.isNullOrEmpty()) {
            return null
        }

        return playlist[0].info.uri
    }

    private fun handlePlay(toPlay: String, ctx: CommandContext) {
        if (toPlay.length > 1024) {
            MessageUtils.sendError(ctx.message)
            sendMsg(ctx, "Input cannot be longer than 1024 characters.")
            return
        }

        ctx.audioUtils.loadAndPlay(ctx.audioData, toPlay, true)
    }

    private fun handlePlay(toPlay: String, variables: Variables, event: SlashCommandInteractionEvent) {
        if (toPlay.length > 1024) {
            event.reply("Input cannot be longer than 1024 characters.").queue()
            return
        }

        // need to wait for this to send actually
        event.reply("Loading your track!").complete()

        variables.audioUtils.loadAndPlay(AudioData.fromSlash(event, variables), toPlay, true)
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

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        if (!event.member!!.voiceState!!.inAudioChannel()) {
            event.reply("Auto-join is not yet supported for slash commands. Sorry about that").queue()
            return
        }

        var toPlay = event.getOption("item")!!.asString

        if (toPlay.contains(pornhubRegex.toRegex()) && !event.channel.isNSFW) {
            event.reply("Because of thumbnails being loaded you can only use PornHub links in channels that are marked as NSFW")
                .queue()
            return
        }

        if (skipParsing) {
            handlePlay(toPlay, variables, event)
            return
        }

        if (!AirUtils.isURL(toPlay) && !toPlay.startsWith("OCR", true)) {
            val songUrl = searchSong(event.guild!!.idLong, toPlay, variables)

            if (songUrl == null) {
                event.reply("No tracks were found").queue()
                return
            }

            toPlay = songUrl
        }

        handlePlay(toPlay, variables, event)
    }
}
