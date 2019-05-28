/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import com.fasterxml.jackson.databind.ObjectMapper
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.objects.config.DunctebotConfig
import org.apache.commons.lang3.StringUtils
import java.net.URLEncoder

@Author(nickname = "duncte123", author = "Duncan Sterken")
class LyricsCommand : MusicCommand() {

    private var authToken = ""
    private val apiBase = "https://api.genius.com"

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val mng = getMusicManager(event.guild, ctx.audioUtils)
        val player = mng.player

        val search: String? = when {
            ctx.args.isNotEmpty() -> ctx.argsRaw
            player.playingTrack != null && !player.playingTrack.info.isStream ->
                player.playingTrack.info.title.trim()
            else -> null
        }

        if (search.isNullOrBlank()) {
            sendMsg(event, "The player is not currently playing anything!")
            return
        }

        searchForSong(search, ctx.config.genius, ctx.variables.jackson) {
            if (it.isNullOrBlank()) {
                sendMsg(event, "There where no lyrics found for the title of this song\n" +
                    "Alternatively you can try `${ctx.prefix}$name <song name>` to search for the lyrics on this song.\n" +
                    "(sometimes the song names in the player are incorrect)")
            } else {
                val url = "https://genius.com$it"
                WebUtils.ins.scrapeWebPage(url).async { doc ->
                    val text = doc.select("div.lyrics").first().child(0).wholeText()
                        .replace("<br>", "\n")

                    sendEmbed(event, EmbedUtils.defaultEmbed()
                        .setTitle("Lyrics for $search", url)
                        .setDescription(StringUtils.abbreviate(text, 1900))
                        .appendDescription("\n\n Full lyrics on [genius.com]($url)")
                        .setFooter("Powered by genius.com", Settings.DEFAULT_ICON)
                        .build())
                }
            }
        }
    }

    override fun help(prefix: String): String? = "Shows the lyrics to the current song"

    override fun getName() = "lyrics"

    private fun getAuthToken(config: DunctebotConfig.Genius, mapper: ObjectMapper): String {
        if (authToken.isBlank()) {
            val formData = HashMap<String, Any>()
            formData["client_id"] = config.client_id
            formData["client_secret"] = config.client_secret
            formData["grant_type"] = "client_credentials"

            val raw = WebUtils.ins.preparePost("$apiBase/oauth/token", formData).execute()

            this.authToken = mapper.readTree(raw).get("access_token").asText("")
        }

        return "Bearer $authToken"
    }

    private fun searchForSong(t: String?, config: DunctebotConfig.Genius, mapper: ObjectMapper, callback: (String?) -> Unit) {
        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
            .header("Authorization", getAuthToken(config, mapper))
            .url("$apiBase/search?q=${URLEncoder.encode(t, "UTF-8")}").build()
        ) { WebParserUtils.toJSONObject(it, mapper) }
            .async {
                val hits = it.get("response").get("hits")
                if (hits.size() < 1) {
                    callback.invoke(null)
                } else {
                    callback.invoke(
                        hits.get(0).get("result").get("path").asText()
                    )
                }
            }
    }
}
