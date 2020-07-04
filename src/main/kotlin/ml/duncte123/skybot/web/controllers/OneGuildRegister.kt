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
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SkyBot
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.adapters.DatabaseAdapter
import ml.duncte123.skybot.objects.config.DunctebotConfig
import ml.duncte123.skybot.objects.web.ModelAndView
import ml.duncte123.skybot.objects.web.WebVariables
import ml.duncte123.skybot.utils.CommandUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import ml.duncte123.skybot.web.WebHelpers
import ml.duncte123.skybot.web.getParamsMap
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.sharding.ShardManager
import spark.Request

@Author(nickname = "duncte123", author = "Duncan Sterken")
object OneGuildRegister {

    fun post(request: Request, shardManager: ShardManager, variables: Variables, mapper: ObjectMapper): Any {
        val params = request.getParamsMap()

        val captcha = params["h-captcha-response"]
        val userId = GuildSettingsUtils.toLong(params["user_id"])
        val guildId = GuildSettingsUtils.toLong(params["guild_id"])

        if (captcha.isNullOrEmpty()) {
            return renderPage(WebVariables().put("message", "Error: Captcha missing"), variables.config)
        }

        val cap = WebHelpers.verifyCapcha(captcha, variables.config.apis.chapta.secret, mapper)

        if (!cap["success"].asBoolean()) {
            return renderPage(WebVariables().put("message", "Could not validate that you are a human"), variables.config)
        }

        if (userId == 0L || guildId == 0L) {
            return renderPage(WebVariables().put("message", "Please fill in all the fields."), variables.config)
        }

        val user: User? = try {
            shardManager.retrieveUserById(userId).complete()
        } catch (e: Exception) {
            println(e)
            null
        }

        @Suppress("FoldInitializerAndIfToElvis") // It's ugly
        if (user == null) {
            return renderPage(WebVariables().put("message", "User with id <b>$userId</b> could not be found"), variables.config)
        }

        val guild: Guild? = shardManager.getGuildById(guildId)

        @Suppress("FoldInitializerAndIfToElvis") // It's ugly
        if (guild == null) {
            return renderPage(WebVariables().put("message", "Server with id <b>$guildId</b> could not be found"), variables.config)
        }

        if (CommandUtils.oneGuildPatrons.containsKey(userId)) {
            return renderPage(WebVariables().put("message", "This user is already registered, please contact a bot admin to have it changed."), variables.config)
        }

        // IMPORTANT: Keep using the one guild endpoint so it doesn't override old patrons
        addOneGuildPatron(user.idLong, guild.idLong, variables.databaseAdapter)

        return renderPage(WebVariables().put("message", "Server successfully registered")
            .put("hideForm", true), variables.config)
    }

    private fun renderPage(map: WebVariables, config: DunctebotConfig): ModelAndView {
        map.put("title", "Register your server for patron perks")
            .put("chapta_sitekey", config.apis.chapta.sitekey)

        return map.toModelAndView("oneGuildRegister.vm")
    }

    // IMPORTANT: Keep using the one guild endpoint so it doesn't override old patrons
    private fun addOneGuildPatron(userId: Long, guildId: Long, database: DatabaseAdapter) {
        database.addOneGuildPatrons(userId, guildId) { _, _ ->
            val instance = SkyBot.getInstance()
            val dbGuild = instance.shardManager.getGuildById(Settings.SUPPORT_GUILD_ID) ?: return@addOneGuildPatrons
            val newPatron = dbGuild.getMemberById(userId) ?: return@addOneGuildPatrons

            val hasRole = newPatron.roles
                .map(Role::getIdLong)
                .any { it == Settings.ONE_GUILD_PATRONS_ROLE }

            if (hasRole) {
                CommandUtils.oneGuildPatrons.put(userId, guildId)
            }
        }
    }

}
