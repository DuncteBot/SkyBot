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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EarthUtils.Companion.sendRedditPost
import ml.duncte123.skybot.utils.MapUtils
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent


@Author(nickname = "duncte123", author = "Duncan Sterken")
class JokeCommand : Command() {

    /*
     * This keeps track of where we are in the jokes
     */
    private val jokeIndex = MapUtils.newLongIntMap()
    private val memeIndex = MapUtils.newLongIntMap()

    init {
        this.displayAliasesInHelp = true
        this.category = CommandCategory.FUN
        this.name = "joke"
        this.aliases = arrayOf("meme")
        this.helpFunction = { _, invoke ->
            when (invoke) {
                "meme" -> "See a funny meme"
                else -> "See a funny joke. Dad's love them!"
            }
        }
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.invoke == "meme") {
            sendRedditPost("memes", memeIndex, ctx.event)

            return
        }

        when (ctx.random.nextInt(2)) {
            0 -> sendRedditPost("Jokes", jokeIndex, ctx.event)
            1 -> sendRanddomJoke(ctx.event)
        }

    }

    private fun sendRanddomJoke(event: GuildMessageReceivedEvent) {
        WebUtils.ins.getJSONObject("https://icanhazdadjoke.com/").async {
            sendEmbed(event, EmbedUtils.embedMessage(it.get("joke").asText()))
        }
    }
}
