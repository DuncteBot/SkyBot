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
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHolder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.User
import org.apache.commons.lang3.RandomStringUtils
import org.apache.http.client.utils.URLEncodedUtils
import spark.ModelAndView
import spark.kotlin.post
import java.nio.charset.StandardCharsets

@Author(nickname = "duncte123", author = "Duncan Sterken")
class OneGuildRegister(private val holder: WebHolder) {

    init {
        holder.get("/register-server", WebVariables()
            .put("title", "Register your server for patron perks")
            .put("chapta_sitekey", holder.config.apis.chapta.sitekey)
            .put("form_id", RandomStringUtils.random(10, true, false)), "oneGuildRegister.twig")

        post("/register-server") {
            val pairs = URLEncodedUtils.parse(request.body(), StandardCharsets.UTF_8)
            val params = holder.toMap(pairs)

            val captcha = params["g-recaptcha-response"]
            val userId = GuildSettingsUtils.toLong(params["user_id"])
            val guildId = GuildSettingsUtils.toLong(params["guild_id"])

            if (captcha.isNullOrEmpty()) {
                return@post renderPage(WebVariables().put("message", "Error: Captcha missing"))
            }

            if (userId == 0L || guildId == 0L) {
                return@post renderPage(WebVariables().put("message", "Please fill in all the fields."))
            }

            val guild: Guild? = holder.shardManager.getGuildById(guildId)
            val user: User? = holder.shardManager.getUserById(userId)

            if (guild == null) {
                return@post renderPage(WebVariables().put("message", "Server with id <b>$guildId</b> could not be found"))
            }

            if (user == null) {
                return@post renderPage(WebVariables().put("message", "User with id <b>$userId</b> could not be found"))
            }

            val cap = holder.helpers.verifyCapcha(captcha, holder.config.apis.chapta.secret)

            if (!cap.getBoolean("success")) {
                return@post renderPage(WebVariables().put("message", "Captcha error: Please try again later"))
            }

            //

            renderPage(WebVariables().put("message", "Server successfully registered").put("hideForm", true))
        }
    }

    private fun renderPage(map: WebVariables): String {
        map.put("title", "Register your server for patron perks")
            .put("chapta_sitekey", holder.config.apis.chapta.sitekey)

        return holder.engine.render(ModelAndView(map.map, "oneGuildRegister.twig"))
    }

}
