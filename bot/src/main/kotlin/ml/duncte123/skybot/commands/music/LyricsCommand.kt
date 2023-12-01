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

import com.github.natanbc.reliqua.limiter.RateLimiter
import me.duncte123.botcommons.StringUtils
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.objects.config.DunctebotConfig
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
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

        val search = playingTrack.info.title.trim()

        // just search for the title, the author might be a weird youtube channel
        handleSearch(search, ctx.config) {
            if (it == null) {
                sendMsg(ctx, "There where no lyrics found for `$search`")
                return@handleSearch
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

            val search = playingTrack.info.title.trim()

            // just search for the title, the author might be a weird youtube channel
            handleSearch(search, variables.config) {
                if (it == null) {
                    event.hook.sendMessage("There where no lyrics found for `$search`").queue()
                    return@handleSearch
                }

                event.hook.sendMessageEmbeds(it.build()).queue()
            }

            return
        }

        event.deferReply().queue()

        val search = opt.asString

        handleSearch(search, variables.config) {
            if (it == null) {
                event.hook.sendMessage("There where no lyrics found for `$search`").queue()
                return@handleSearch
            }

            event.hook.sendMessageEmbeds(it.build()).queue()
        }
    }

    private fun handleSearch(search: String, config: DunctebotConfig, cb: (EmbedBuilder?) -> Unit) {
        searchForSong(search, config) {
            if (it == null) {
                cb(null)
                return@searchForSong
            }

            cb(
                EmbedUtils.getDefaultEmbed()
                    .setTitle("Lyrics for $search", it.url)
                    .setThumbnail(it.art)
                    .setDescription(StringUtils.abbreviate(it.lyrics, 1900))
                    .appendDescription("\n\n Full lyrics on [genuis.com](${it.url})")
                    .setFooter("Powered by genuis.com")
            )
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
                    .replace("\n\n\n", "\n\n")
                    .trim()

                callback(text)
            }) {
                callback(null)
            }
    }

    private data class LyricInfo(val art: String, val url: String, val lyrics: String)
}
