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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "duncte123", author = "Duncan Sterken")
class JokeCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "joke"
        this.helpFunction = { _, _ -> "See a funny joke. Dad's love them!"}
    }

    override fun execute(ctx: CommandContext) {
        when (ctx.random.nextInt(2)) {
            0 -> sendJokeFromApi(ctx)
            1 -> sendRanddomJoke(ctx.event)
        }
    }

    private fun sendJokeFromApi(ctx: CommandContext) {
        val json = ctx.apis.executeDefaultGetRequest("joke", false).get("data")

        val embed = EmbedUtils.defaultEmbed()
            .setTitle(json.get("title").asText(), json.get("url").asText())
            .setDescription(json.get("body").asText())

        sendEmbed(ctx, embed)
    }

    private fun sendRanddomJoke(event: GuildMessageReceivedEvent) {
        WebUtils.ins.getJSONObject("https://icanhazdadjoke.com/").async {
            sendEmbed(event, EmbedUtils.embedMessage(it.get("joke").asText()))
        }
    }
}
