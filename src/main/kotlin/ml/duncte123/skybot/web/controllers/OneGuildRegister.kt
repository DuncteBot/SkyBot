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

package ml.duncte123.skybot.web.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.WebVariables
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.utils.GuildUtils
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.getParamsMap
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.sharding.ShardManager
import spark.ModelAndView
import spark.Request

@Author(nickname = "duncte123", author = "Duncan Sterken")
object OneGuildRegister {

    fun post(request: Request, shardManager: ShardManager, variables: Variables, mapper: ObjectMapper): Any {
        val params = request.getParamsMap()

        val captcha = params["g-recaptcha-response"]
        val userId = GuildSettingsUtils.toLong(params["user_id"])
        val guildId = GuildSettingsUtils.toLong(params["guild_id"])

        if (captcha.isNullOrEmpty()) {
            return renderPage(WebVariables().put("message", "Error: Captcha missing"), variables.config)
        }

        if (userId == 0L || guildId == 0L) {
            return renderPage(WebVariables().put("message", "Please fill in all the fields."), variables.config)
        }

        val guild: Guild? = shardManager.getGuildById(guildId)
        val user: User? = shardManager.getUserById(userId)

        if (guild == null) {
            return renderPage(WebVariables().put("message", "Server with id <b>$guildId</b> could not be found"), variables.config)
        }

        if (user == null) {
            return renderPage(WebVariables().put("message", "User with id <b>$userId</b> could not be found"), variables.config)
        }

        if (CommandUtils.oneGuildPatrons.containsKey(userId)) {
            return renderPage(WebVariables().put("message", "This user is already registered, please contact a bot admin to have it changed."), variables.config)
        }

        val cap = WebHelpers.verifyCapcha(captcha, variables.config.apis.chapta.secret, mapper)

        if (!cap.get("success").asBoolean()) {
            return renderPage(WebVariables().put("message", "Captcha error: Please try again later"), variables.config)
        }

        GuildUtils.addOneGuildPatron(user.idLong, guild.idLong, variables)

        return renderPage(WebVariables().put("message", "Server successfully registered")
            .put("hideForm", true), variables.config)
    }

    private fun renderPage(map: WebVariables, config: DunctebotConfig): ModelAndView {
        map.put("title", "Register your server for patron perks")
            .put("chapta_sitekey", config.apis.chapta.sitekey)

        return map.toModelAndView("oneGuildRegister.twig")
    }

}
