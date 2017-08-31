package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class KpopCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        try {
            String query = "";

            if(args.length > 0) {
                query = "?search=" + StringUtils.join(args, " ");
            }

            String url = Config.apiBase + "/kpop.php" + query;
            Document raw = Jsoup.connect(url).get();

            String id = raw.body().select("id").text();
            String name = raw.body().select("name").text();
            String group = raw.body().select("band").text();
            String imgUrl = raw.body().select("picture").text();

            EmbedBuilder eb = AirUtils.defaultEmbed()
                    .setDescription("Here is a kpop member from the group " + group)
                    .addField("Name of the member", name, false)
                    .setImage(imgUrl)
                    .setFooter("Query id: " + id, Config.defaultIcon);
            event.getChannel().sendMessage(eb.build()).queue();
        }
        catch (Exception e) {
            event.getChannel().sendMessage("SCREAM THIS TO _duncte123#1245_: " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Gives you a random kpop member, command idea by Exa\nUsage: " + Config.prefix + "kpop [search term]";
    }
}
