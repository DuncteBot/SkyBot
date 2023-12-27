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

import com.github.natanbc.reliqua.limiter.RateLimiter
import dev.arbjerg.lavalink.client.Link
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.lyrics.model.Lyrics
import me.duncte123.lyrics.model.TextLyrics
import me.duncte123.lyrics.model.TimedLyrics
import me.duncte123.skybot.Variables
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.MusicCommand
import me.duncte123.skybot.objects.config.DunctebotConfig
import me.duncte123.skybot.utils.chunkForEmbed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import reactor.core.scheduler.Schedulers
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LyricsCommand : MusicCommand() {
    init {
        this.justRunLmao = true

        this.name = "lyrics"
        this.help = "Search for song lyrics or show the ones for the currently playing song"
        this.usage = "[song name]"
    }

    override fun run(ctx: CommandContext) {
        val args = ctx.args

        if (args.isNotEmpty()) {
            // TODO: search with lavalink for lyrics
            handleSearch(ctx.argsRaw, ctx.config) {
                if (it == null) {
                    sendMsg(ctx, "There where no lyrics found for `${ctx.argsRaw}`")
                    return@handleSearch
                }

                sendEmbed(ctx, it)
            }
            return
        }

        val player = ctx.audioUtils.getMusicManager(ctx.guildId).player
        val playingTrack = player.currentTrack

        if (playingTrack == null) {
            sendMsg(ctx, "The player is not currently playing anything!")
            return
        }

        loadLyricsFromLavalink(player.link) {
            if (it == null) {
        // TODO: fallback for genius
                val searchItem = "${playingTrack.info.title} - ${playingTrack.info.author}"

                handleSearch(searchItem, ctx.config) { embed ->
                    if (embed == null) {
                        sendMsg(ctx, "There where no lyrics found for `${playingTrack.info.title}`")
                        return@handleSearch
                    }

                    sendEmbed(ctx, embed)
                }
                return@loadLyricsFromLavalink
            }

            sendEmbed(ctx, it)
        }
    }

    override fun getSubData(): SubcommandData {
        return super.getSubData()
            .addOption(
                OptionType.STRING,
                "song",
                "The song to search for",
                false
            )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        val opt = event.getOption("song")

        if (opt == null) {
            val player = variables.audioUtils.getMusicManager(event.guild!!.idLong).player
            val playingTrack = player.currentTrack

            if (playingTrack == null) {
                event.reply("The player is not currently playing anything!").queue()
                return
            }

            event.deferReply().queue()

            loadLyricsFromLavalink(player.link) {
                if (it == null) {
                    val searchItem = "${playingTrack.info.title} - ${playingTrack.info.author}"

                    handleSearch(searchItem, variables.config) { embed ->
                        if (embed == null) {
                            event.hook.sendMessage("There where no lyrics found for `${playingTrack.info.title}`")
                                .queue()
                            return@handleSearch
                        }

                        event.hook.sendMessageEmbeds(embed.build()).queue()
                    }
                    return@loadLyricsFromLavalink
                }

                event.hook.sendMessageEmbeds(it.build()).queue()
            }
            return
        }

        event.deferReply().queue()

        val search = opt.asString

        // TODO: search with lavalink for lyrics
        handleSearch(search, variables.config) {
            if (it == null) {
                event.hook.sendMessage("There where no lyrics found for `$search`").queue()
                return@handleSearch
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
            .publishOn(Schedulers.boundedElastic())
            .doOnError { cb(null) }
            .doOnSuccess {
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
                            text
                        )
                    }

                    is TextLyrics -> LyricInfo(
                        it.track.albumArt.last().url,
                        it.track.title,
                        null,
                        it.text
                    )

                    else -> null
                }

                lyricInfo?.let { info ->
                    cb(buildLyricsEmbed(info))
                }
            }
            .subscribe()
    }

    private fun buildLyricsEmbed(data: LyricInfo): EmbedBuilder {
        val builder = EmbedUtils.getDefaultEmbed()
            .setTitle("Lyrics for ${data.title}", data.url)
            .setThumbnail(data.artUrl)

        data.lyrics.chunkForEmbed(450).forEachIndexed { index, chunk ->
            builder.addField("**[${index + 1}]**", chunk, true)
        }

        return builder
    }

    private fun handleSearch(search: String, config: DunctebotConfig, cb: (EmbedBuilder?) -> Unit) {
        searchForSong(search, config) {
            if (it == null) {
                cb(null)
                return@searchForSong
            }

            cb(buildLyricsEmbed(it))
        }
    }

    private fun searchForSong(search: String, config: DunctebotConfig, callback: (LyricInfo?) -> Unit) {
        WebUtils.ins.prepareBuilder(
            WebUtils.defaultRequest()
                .header("Authorization", "Bearer ${config.apis.genius}")
                .url("https://api.genius.com/search?q=${URLEncoder.encode(search, StandardCharsets.UTF_8)}"),
            {
                it.setRateLimiter(WebUtils.ins.getRateLimiter("api.genius.com/search"))
            },
            null
        ).build(
            WebParserUtils::toJSONObject,
            WebParserUtils::handleError
        ).async {
            val results = it["response"]["hits"]

            if (results.isEmpty) {
                callback(null)
                return@async
            }

            val firstResult = results.firstOrNull { node -> node["type"].asText() == "song" }

            if (firstResult == null) {
                callback(null)
                return@async
            }

            val data = firstResult["result"]
            val path = data["path"].asText()

            loadLyrics(path) { lyrics ->
                if (lyrics.isNullOrEmpty()) {
                    callback(null)
                } else {
                    callback(
                        LyricInfo(
                            data["song_art_image_url"].asText(),
                            "",
                            data["url"].asText(),
                            lyrics
                        )
                    )
                }
            }
        }
    }

    private fun loadLyrics(path: String, callback: (String?) -> Unit) {
        WebUtils.ins.scrapeWebPage("https://genius.com/amp$path") { it.setRateLimiter(RateLimiter.directLimiter()) }
            .async({
                val lyricsContainer = it.select("div.lyrics")
                val text = lyricsContainer.first()!!
                    .wholeText()
                    .replace("<br>", "\n")
                    .replace("\n\n\n", "\n")
                    .trim()

                callback(text)
            }) {
                callback(null)
            }
    }

    private data class LyricInfo(val artUrl: String, val title: String, val url: String?, val lyrics: String)
}
