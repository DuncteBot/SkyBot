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

package ml.duncte123.skybot.commands.`fun`

import com.github.natanbc.reliqua.limiter.RateLimiter
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.extensions.abbreviate
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.MessageEmbed

class JokeCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "joke"
        this.help = "See a funny joke. Dad's love them!"
    }

    override fun execute(ctx: CommandContext) {
        when (ctx.random.nextInt(2)) {
            0 -> sendJokeFromApi(ctx)
            1 -> sendRanddomJoke(ctx)
        }
    }

    private fun sendJokeFromApi(ctx: CommandContext) {
        val json = ctx.apis.executeDefaultGetRequest("joke", false)["data"]

        val embed = EmbedUtils.getDefaultEmbed()
            .setTitle(json["title"].asText().abbreviate(MessageEmbed.TITLE_MAX_LENGTH), json["url"].asText())
            .setDescription(json["body"].asText().abbreviate(MessageEmbed.TEXT_MAX_LENGTH))

        sendEmbed(ctx, embed)
    }

    private fun sendRanddomJoke(ctx: CommandContext) {
        WebUtils.ins.getJSONObject("https://icanhazdadjoke.com/") { it.setRateLimiter(RateLimiter.directLimiter()) }.async {
            sendEmbed(ctx, EmbedUtils.embedMessage(it["joke"].asText()))
        }
    }
}
