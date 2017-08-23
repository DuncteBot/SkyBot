package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class KpopCommand extends Command {
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            String url = Config.apiBase + "/kpop.php";
            Document raw = Jsoup.connect(url).get();

            String name = raw.body().select("name").text();
            String group = raw.body().select("band").text();
            String imgUrl = raw.body().select("picture").text();

            EmbedBuilder eb = AirUtils.defaultEmbed()
                    .setDescription("Here is a kpop member from the group " + group)
                    .addField("Name of the member", name, false)
                    .setImage(imgUrl);
            event.getChannel().sendMessage(eb.build()).queue();
        }
        catch (Exception e) {
            event.getChannel().sendMessage("SCREAM THIS TO _duncte123#1245_: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "Gives you a random kpop member, command idea by Exa";
    }
}
