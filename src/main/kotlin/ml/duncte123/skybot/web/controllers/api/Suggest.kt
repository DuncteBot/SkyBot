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

package ml.duncte123.skybot.web.controllers.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.WebHelpers.addTrelloCard
import spark.Request
import spark.Response

object Suggest {

    private fun JsonNode.hasAll(vararg fieldNames: String): Boolean {
        return fieldNames.all { this.has(it) }
    }

    fun create(request: Request, response: Response, config: DunctebotConfig, mapper: ObjectMapper): Any {
        return try {
            val jsonBody = mapper.readTree(request.bodyAsBytes())

            if (!jsonBody.hasAll("name", "sug", "desc", "g-recaptcha-response")) {
                response.status(400)

                return mapper.createObjectNode()
                    .put("status", "failure")
                    .put("message", "missing_input")
                    .put("code", response.status())
            }

            val captcha = jsonBody["g-recaptcha-response"].asText()
            val name = jsonBody["name"].asText("")
            val suggestion = jsonBody["sug"].asText("")
            val description = jsonBody["desc"].asText("")

            if (name.isNullOrEmpty() || suggestion.isNullOrEmpty()) {
                response.status(400)

                return mapper.createObjectNode()
                    .put("status", "failure")
                    .put("message", "missing_input")
                    .put("code", response.status())
            }

            val cap = WebHelpers.verifyCapcha(captcha, config.apis.chapta.secret, mapper)

            if (!cap["success"].asBoolean()) {
                return mapper.createObjectNode()
                    .put("status", "failure")
                    .put("message", "captcha_failed")
                    .put("code", response.status())
            }

            val extraDesc = if (!description.isNullOrEmpty()) "$description\n\n" else ""
            val descText = "${extraDesc}Suggested by: $name\nSuggested from website"

            val url = addTrelloCard(suggestion.toString(), descText, config.apis.trello, mapper)["shortUrl"].asText()

            return mapper.createObjectNode()
                .put("status", "success")
                .put("trello_url", url)
                .put("code", response.status())

        } catch (jse: Exception) {
            response.status(400)

            mapper.createObjectNode()
                .put("status", "failure")
                .put("message", "invalid_json")
                .put("code", response.status())
        }
    }

}
