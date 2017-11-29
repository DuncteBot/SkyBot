/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.json.JSONArray
import org.json.JSONObject

import java.util.stream.Collectors

class JokeCommand extends Command {

    /**
     * This keeps track of where we are in the jokes
     */
    private Map<String, Integer> jokeIndex

    JokeCommand() {
        this.category = CommandCategory.FUN
        this.jokeIndex = new TreeMap<>()
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        String rawJSON = WebUtils.getText("https://www.reddit.com/r/Jokes/top/.json?sort=top&t=day&limit=400")
        JSONObject jsonObject = new JSONObject(rawJSON)

        def posts = jsonObject.getJSONObject("data").getJSONArray("children").toList().parallelStream().filter{
            !it["data"]["preview"] && ((String) it["data"]["selftext"]).size() <= 550 && ((String) it["data"]["title"]).size() <= 256
        }.collect(Collectors.toList())

        if (!jokeIndex.containsKey(event.guild.id) || jokeIndex.get(event.guild.id) >= posts.size()) {
            println "pls only do this once"
            jokeIndex.put(event.guild.id, 0)
        }

        def jokeI = jokeIndex.get(event.guild.id)

        JSONObject jokeData = ((JSONArray) jsonObject["data"]["children"]).getJSONObject(jokeI).getJSONObject("data")
        jokeIndex.put(event.guild.id, jokeI + 1)
        String title = jokeData["title"]
        String text = jokeData["selftext"]
        String url = jokeData["url"]

        sendEmbed(event, EmbedUtils.defaultEmbed().setTitle(title, url).setDescription(text).build())
    }

    @Override
    String help() {
        return "See a funny joke. Dad's love them!\n" +
                "Usage: `$PREFIX$name`"
    }

    @Override
    String getName() {
        return "joke"
    }
}
