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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.`fun`

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONObject
import java.util.*
import java.util.stream.Collectors

class JokeCommand : Command() {

    /**
     * This keeps track of where we are in the jokes
     */
    private val jokeIndex: MutableMap<String, Int>

    init {
        this.category = CommandCategory.FUN
        this.jokeIndex = TreeMap()
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        val rawJSON = WebUtils.getText("https://www.reddit.com/r/Jokes/top/.json?sort=top&t=day&limit=400")
        val jsonObject = JSONObject(rawJSON)

        val posts = jsonObject.getJSONObject("data").getJSONArray("children").filter({ it as JSONObject
                    (it.getJSONObject("data").getString("selftext").length <= 550
                    && it.getJSONObject("data").getString("title").length <= 256)
        })

        if (!jokeIndex.containsKey(event.guild.id) || jokeIndex.getOrDefault(event.guild.id, 0) >= posts.size) {
            jokeIndex.put(event.guild.id, 0)
        }

        val jokeI = jokeIndex.getOrDefault(event.guild.id, 0)

        val jokeData: JSONObject = jsonObject.getJSONObject("data").getJSONArray("children").getJSONObject(jokeI).getJSONObject("data")
        jokeIndex.put(event.guild.id, jokeI + 1)
        val title: String = jokeData.getString("title")
        val text: String = jokeData.getString("selftext")
        val url: String = jokeData.getString("url")

        sendEmbed(event, EmbedUtils.defaultEmbed().setTitle(title, url).setDescription(text).build())

    }

    override fun help() = "See a funny joke. Dad's love them!\n" +
            "Usage: `$PREFIX$name`"

    override fun getName() = "joke"
}