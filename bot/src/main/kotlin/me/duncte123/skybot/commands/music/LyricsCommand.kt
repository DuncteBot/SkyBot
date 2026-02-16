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

import dev.arbjerg.lavalink.client.LavalinkNode
import dev.arbjerg.lavalink.client.Link
import fredboat.audio.player.LavalinkManager
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.lyrics.model.Lyrics
import me.duncte123.lyrics.model.TextLyrics
import me.duncte123.lyrics.model.TimedLyrics
import me.duncte123.skybot.Variables
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.utils.chunkForEmbed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

class LyricsCommand : MusicCommand() {
    init {
        this.name = "lyrics"
        this.help = "Search for song lyrics or show the ones for the currently playing song"
        this.usage = "[song name]"
    }

    override fun run(ctx: CommandContext) {
        val args = ctx.args

        if (args.isNotEmpty()) {
            val randomNode = LavalinkManager.INS.lavalink.nodes.random()

            searchForLyrics(randomNode, ctx.argsRaw) {
                if (it == null) {
                    sendMsg(ctx, "There where no lyrics found for `${ctx.argsRaw}`")
                    return@searchForLyrics
                }

                sendEmbed(ctx, it)
            }
            return
        }

        val mng = ctx.audioUtils.getMusicManager(ctx.guildId)
        mng.latestChannelId = ctx.channel.idLong
        val player = mng.player.getOrNull()
        val playingTrack = player?.track

        if (playingTrack == null) {
            sendMsg(ctx, "The player is not currently playing anything!")
            return
        }

        loadLyricsFromLavalink(mng.link!!) {
            if (it == null) {
                sendMsg(ctx, "There where no lyrics found for `${playingTrack.info.title}`")
                return@loadLyricsFromLavalink
            }

            sendEmbed(ctx, it)
        }
    }

    override fun getSubData(): SubcommandData = super.getSubData()
        .addOption(
            OptionType.STRING,
            "song",
            "The song to search for",
            false
        )

    override fun handleEvent(
        event: SlashCommandInteractionEvent,
        guild: DunctebotGuild,
        variables: Variables,
    ) {
        val opt = event.getOption("song")

        if (opt == null) {
            val mng = variables.audioUtils.getMusicManager(event.guild!!.idLong)
            mng.latestChannelId = event.channel.idLong
            val player = mng.player.getOrNull()
            val playingTrack = player?.track

            if (playingTrack == null) {
                event.reply("The player is not currently playing anything!").queue()
                return
            }

            event.deferReply().queue()

            loadLyricsFromLavalink(mng.link!!) {
                if (it == null) {
                    event.hook.sendMessage("There where no lyrics found for `${playingTrack.info.title}`")
                        .queue()
                    return@loadLyricsFromLavalink
                }

                event.hook.sendMessageEmbeds(it.build()).queue()
            }
            return
        }

        event.deferReply().queue()

        val search = opt.asString
        val randomNode = LavalinkManager.INS.lavalink.nodes.random()

        searchForLyrics(randomNode, search) {
            if (it == null) {
                event.hook.sendMessage("There where no lyrics found for `$search`").queue()
                return@searchForLyrics
            }

            event.hook.sendMessageEmbeds(it.build()).queue()
        }
    }

    private fun loadLyricsFromLavalink(link: Link, cb: (EmbedBuilder?) -> Unit) {
        val sessionId = link.node.sessionId!!
        val guildId = link.guildId

        link.node.customJsonRequest(Lyrics::class.java) {
            it.path("/v4/sessions/$sessionId/players/$guildId/lyrics")
        }
            .subscribe({
                val lyricInfo = when (it) {
                    is TimedLyrics -> {
                        // Block is safe here, player is already cached
                        val position = link.getPlayer().block()!!.state.position

                        val text = buildString {
                            it.lines.forEach { line ->
                                if (line.range.start <= position && position <= line.range.end) {
                                    append("__**${line.line}**__\n")
                                } else {
                                    append("${line.line}\n")
                                }
                            }
                        }

                        LyricInfo(
                            it.track.albumArt.last().url,
                            it.track.title,
                            null,
                            it.source,
                            text
                        )
                    }

                    is TextLyrics -> LyricInfo(
                        it.track.albumArt.last().url,
                        it.track.title,
                        null,
                        it.source,
                        it.text
                    )

                    else -> null
                }

                lyricInfo?.let { info ->
                    cb(buildLyricsEmbed(info))
                }
            }) {
                LOGGER.error("Failed to generate lyrics embed", it)
                cb(null)
            }
    }

    private fun searchForLyrics(node: LavalinkNode, q: String, cb: (EmbedBuilder?) -> Unit) {
        node.customJsonRequest(Lyrics::class.java) {
            it.path("/v4/lyrics/search?source=genius&query=$q")
        }
            .subscribe({
                it as TextLyrics // We always get text lyrics here since we are using genius.

                cb(
                    buildLyricsEmbed(
                        LyricInfo(
                            it.track.albumArt.last().url,
                            it.track.title,
                            null,
                            it.source,
                            it.text
                        )
                    )
                )
            }) {
                LOGGER.error("Failed searching lyrics for genius", it)
                cb(null)
            }
    }

    private fun buildLyricsEmbed(data: LyricInfo): EmbedBuilder {
        val builder = EmbedUtils.getDefaultEmbed()
            .setTitle("Lyrics for ${data.title}", data.url)
            .setThumbnail(data.artUrl)

        val lyrics = data.lyrics
        val trimmedLyrics = lyrics.substring(
            0 until min(lyrics.length, 5800) // seems like a good max length
        )

        trimmedLyrics.chunkForEmbed(450).forEachIndexed { index, chunk ->
            builder.addField("**[${index + 1}]**", chunk, true)
        }

        builder.setFooter("Source: ${data.source}")

        return builder
    }

    private data class LyricInfo(val artUrl: String, val title: String, val url: String?, val source: String, val lyrics: String)
}
