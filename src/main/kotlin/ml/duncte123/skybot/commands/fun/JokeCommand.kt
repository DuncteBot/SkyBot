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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.BuildConfig.URL_ARRAY
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EarthUtils.Companion.sendRedditPost
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.Variables.RAND
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.util.*

class JokeCommand : Command() {

    /*
     * This keeps track of where we are in the jokes
     */
    private val jokeIndex: MutableMap<String, Int>

    init {
        this.category = CommandCategory.FUN
        this.jokeIndex = TreeMap()
    }

    override fun executeCommand(ctx: CommandContext) {

        when (RAND.nextInt(2)) {
            0 -> sendRedditPost("Jokes", jokeIndex, ctx.event)
            1 -> sendRanddomJoke(ctx.event)
        }

    }

    override fun help() = "See a funny joke. Dad's love them!\n" +
            "Usage: `$PREFIX$name`"

    override fun getName() = "joke"

    override fun getAliases() = arrayOf("meme")

    private fun sendRanddomJoke(event: GuildMessageReceivedEvent) {
        WebUtils.ins.getJSONObject(URL_ARRAY[1]).async {
            sendEmbed(event, EmbedUtils.embedMessage(it.getString("joke")))
        }
    }
}