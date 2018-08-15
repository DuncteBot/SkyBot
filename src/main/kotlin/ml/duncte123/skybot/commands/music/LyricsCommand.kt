/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botCommons.web.WebUtils
import me.duncte123.botCommons.web.WebUtilsErrorUtils
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import java.net.URLEncoder

class LyricsCommand : MusicCommand() {

    private var authToken: String = ""
    private val apiBase = "https://api.genius.com"

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!hasUpvoted(event.author, ctx.config)) {
            MessageUtils.sendEmbed(event, EmbedUtils.embedMessage(
                    "I'm sorry but you can't use this feature because you haven't up-voted the bot." +
                            " You can up-vote the bot and get access to this feature [here](https://discordbots.org/bot/210363111729790977" +
                            ") or become a patreon [here](https://patreon.com/duncte123)"))
        } else if (channelChecks(event)) {
            val mng = getMusicManager(event.guild)
            val player = mng.player
            val search: String? = when {
                !ctx.args.isEmpty() -> ctx.rawArgs
                player.playingTrack != null && !player.playingTrack.info.isStream ->
                    player.playingTrack.info.title.replace("[OFFICIAL VIDEO]", "").trim()
                else -> null
            }
            if (search.isNullOrBlank()) {
                MessageUtils.sendMsg(event, "The player is not currently playing anything!")
                return
            }
            searchForSong(search, ctx.config.genius) {
                if (it.isNullOrBlank()) {
                    MessageUtils.sendMsg(event, "There where no lyrics found for the title of this song\n" +
                            "Alternatively you can try `$PREFIX$name song name` to search for the lyrics on this soing.\n" +
                            "(sometimes the song names in the player are wrong)")
                } else {
                    val url = "https://genius.com$it"
                    WebUtils.ins.scrapeWebPage(url).async { doc ->
                        val text = doc.select("div.lyrics").first().child(0).wholeText()
                                .replace("<br>", "\n")
                        MessageUtils.sendEmbed(event, EmbedUtils.defaultEmbed()
                                .setTitle("Lyrics for $search", url)
                                .setDescription(StringUtils.abbreviate(text, 1900))
                                .appendDescription("\n\n Full lyrics on [genius.com]($url)")
                                .setFooter("Powered by genius.com", Settings.DEFAULT_ICON)
                                .build())
                    }
                }
            }
        }
    }

    override fun help() = "Shows the lyrics to the current song"

    override fun getName() = "lyrics"

    private fun getAuthToken(config: DunctebotConfig.Genius): String {
        if (authToken.isBlank()) {
            val formData = HashMap<String, Any>()
            formData["client_id"] = config.client_id
            formData["client_secret"] = config.client_secret
            formData["grant_type"] = "client_credentials"
            val raw = WebUtils.ins.preparePost("$apiBase/oauth/token", formData).execute()
            this.authToken = JSONObject(raw).optString("access_token")
        }
        return "Bearer $authToken"
    }

    private fun searchForSong(t: String?, config: DunctebotConfig.Genius, callback: (String?) -> Unit) {
        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
                .header("Authorization", getAuthToken(config))
                .url("$apiBase/search?q=${URLEncoder.encode(t, "UTF-8")}").build(),
                WebUtilsErrorUtils::toJSONObject).async {
            val hits = it.getJSONObject("response").getJSONArray("hits")
            if (hits.length() < 1) {
                callback.invoke(null)
            } else {
                callback.invoke(
                        hits.getJSONObject(0).getJSONObject("result").getString("path")
                )
            }
        }
    }
}
