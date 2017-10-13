package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.URLConnectionReader;
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
            String rawJSON = URLConnectionReader.getText("https://www.reddit.com/r/Jokes/top/.json?sort=top&t=day&limit=400");
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
