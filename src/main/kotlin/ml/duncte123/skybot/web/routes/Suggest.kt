/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.web.routes

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.web.WebHolder
import org.apache.http.client.utils.URLEncodedUtils
import spark.ModelAndView
import spark.Spark.*
import spark.kotlin.*
import java.nio.charset.Charset

@Author(nickname = "duncte123", author = "Duncan Sterken")
class Suggest(private val holder: WebHolder) {

    init {
        holder.get("/suggest", WebVariables().put("title", "Leave a suggestion")
            .put("chapta_sitekey", holder.config.apis.chapta.sitekey), "suggest.twig")

        post("/suggest") {
            val pairs = URLEncodedUtils.parse(request.body(), Charset.defaultCharset())
            val params = holder.toMap(pairs)

            val captcha = params["g-recaptcha-response"] + ""
            val name = params["name"]
            val suggestion = params["sug"]
            val description = params["desc"]

            if (name.isNullOrEmpty() || suggestion.isNullOrEmpty()) {
                return@post renderSugPage(WebVariables().put("message", "Please fill in all the fields."))
            }

            val cap = holder.helpers.verifyCapcha(captcha, holder.config.apis.chapta.secret)

            if (!cap.getBoolean("success")) {
                return@post renderSugPage(WebVariables().put("message", "Captcha error: Please try again later"))
            }

            val extraDesc = if (!description.isNullOrEmpty()) "$description\n\n" else ""
            val descText = "${extraDesc}Suggested by: $name\nSuggested from website"

            val url = holder.helpers
                .addTrelloCard(suggestion.toString(), descText, holder.config.apis.trello).getString("shortUrl")

            renderSugPage(WebVariables().put("message", "Thanks for submitting, you can view your suggestion <a target='_blank' href='$url'>here</a>"))
        }
    }

    private fun renderSugPage(map: WebVariables): String {
        map.put("title", "Leave a suggestion")
            .put("chapta_sitekey", holder.config.apis.chapta.sitekey)

        return holder.engine.render(ModelAndView(map.map, "suggest.twig"))
    }

}
