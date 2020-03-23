/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.music

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.FormRequestBody
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.objects.config.DunctebotConfig
import org.apache.commons.lang3.StringUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Author(nickname = "duncte123", author = "Duncan Sterken")
class LyricsCommand : MusicCommand() {

    private var authToken = ""
    private val apiBase = "https://api.genius.com"

    init {
        this.name = "lyrics"
        this.help = "Search for song lyrics or show the ones for the currently playing song"
        this.usage = "[song name]"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isNotEmpty()) {
            handleSearch(ctx.argsRaw, ctx)
            return
        }

        val mng = ctx.audioUtils.getMusicManager(ctx.guild)
        val player = mng.player

        if (player.playingTrack == null) {
            sendMsg(ctx, "The player is not currently playing anything!")
            return
        }

        handleSearch(player.playingTrack.info.title.trim(), ctx)
    }

    private fun handleSearch(search: String, ctx: CommandContext) {
        searchForSong(search, ctx.config.genius) {
            if (it.isNullOrBlank()) {
                sendMsg(ctx, "There where no lyrics found for `$search`")
            } else {
                val url = "https://genius.com$it"
                WebUtils.ins.scrapeWebPage(url).async { doc ->
                    val text = doc.select("div.lyrics").first().child(0).wholeText()
                        .replace("<br>", "\n")

                    sendEmbed(ctx, EmbedUtils.defaultEmbed()
                        .setTitle("Lyrics for $search", url)
                        .setDescription(StringUtils.abbreviate(text, 1900))
                        .appendDescription("\n\n Full lyrics on [genius.com]($url)")
                        .setFooter("Powered by genius.com", Settings.DEFAULT_ICON)
                        .build())
                }
            }
        }
    }

    private fun getAuthToken(config: DunctebotConfig.Genius): String {
        if (authToken.isBlank()) {
            val formData = FormRequestBody()
            formData.append("client_id", config.client_id)
            formData.append("client_secret", config.client_secret)
            formData.append("grant_type", "client_credentials")

            val json = WebUtils.ins.postRequest("$apiBase/oauth/token", formData)
                .build(WebParserUtils::toJSONObject, WebParserUtils::handleError)
                .execute()

            this.authToken = json["access_token"].asText("")
        }

        return "Bearer $authToken"
    }

    private fun searchForSong(t: String?, config: DunctebotConfig.Genius, callback: (String?) -> Unit) {
        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
            .header("Authorization", getAuthToken(config))
            .url("$apiBase/search?q=${URLEncoder.encode(t, StandardCharsets.UTF_8)}").build(),
            WebParserUtils::toJSONObject
        ).async {
            val hits = it["response"]["hits"]
            if (hits.isEmpty) {
                callback.invoke(null)
            } else {
                callback.invoke(
                    hits[0]["result"]["path"].asText()
                )
            }
        }
    }
}
