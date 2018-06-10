package ml.duncte123.skybot.commands.fun;

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;
import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class UrbanCommand extends Command {

    public UrbanCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {

        if(args.length < 1) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + " <search term>`");
            return;
        }

        String term = StringUtils.join(args, " ");
        String url = "http://api.urbandictionary.com/v0/define?term=" + term;
//        String webUrl = "https://www.urbandictionary.com/define.php?term=" + term;
        WebUtils.ins.getJSONObject(url).async( json -> {
            if(json.getJSONArray("list").length() < 1) {
                sendMsg(event, "Nothing found");
                return;
            }
            String tags = "`" + StringUtils.join(json.getJSONArray("tags"), "`, `") + "`";
            JSONObject item = json.getJSONArray("list").getJSONObject(0);
            String permaLink = item.getString("permalink");

            EmbedBuilder eb = EmbedUtils.defaultEmbed()
//                    .setTitle("term", webUrl)
                    .setAuthor("Author: " + item.getString("author"))
                    .setDescription("_TOP DEFINITION:_\n\n")
                    .appendDescription(item.getString("definition"))
                    .appendDescription("\n\n")
                    .addField("Example", item.getString("example"), false)
                    .addField("Upvotes:", item.getInt("thumbs_up") + "", true)
                    .addField("Downvotes:", item.getInt("thumbs_down") + "", true)
                    .addField("Link:", "[" + permaLink + "](" + permaLink + ")", false)
                    .addField("Tags:", tags, false)
                    ;
            sendEmbed(event, eb.build());
        });

    }

    @Override
    public String help() {
        return "Search the urban dictionary.\n" +
                "Usage: `" + PREFIX + getName() + " <search term>`";
    }

    @Override
    public String getName() {
        return "urban";
    }
}
