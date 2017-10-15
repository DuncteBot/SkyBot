/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class JokeCommand extends Command {

    /**
     * This keeps track of where we are in the jokes
     */
    private Map<String, Integer> jokeIndex;

    public JokeCommand() {
        this.jokeIndex = new TreeMap<>();
    }

    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        String guildId = event.getGuild().getId();

        try {
            String rawJSON = WebUtils.getText("https://www.reddit.com/r/Jokes/top/.json?sort=top&t=day&limit=400");
            JSONObject jsonObject = new JSONObject(rawJSON);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");

            if(!jokeIndex.containsKey(guildId) || jokeIndex.get(guildId) >= children.length()){
                jokeIndex.put(guildId, 0);
            }

            int jokeI = jokeIndex.get(guildId);

            JSONObject postData = children.getJSONObject(jokeI).getJSONObject("data");

            jokeIndex.put(guildId, jokeI + 1);
            //System.out.println(postData);
            String title = postData.getString("title");
            String text = postData.getString("selftext");
            String url = postData.getString("url");

            sendEmbed(event, EmbedUtils.defaultEmbed().setTitle(title, url).setDescription(text).build());

        }
        catch (Exception e) {
            sendMsg(event, "ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "See a funny joke. Dad's love them!";
    }

    @Override
    public String getName() {
        return "joke";
    }
}
