package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class KpopCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        String id = "";
        String name = "";
        String group = "";
        String imgUrl = "";
        try {
            String dbName = AirUtils.db.getName();
            Connection database = AirUtils.db.getConnection();
            database.createStatement().execute("SET CHARACTER SET utf8");

            if(args.length > 0) {

                PreparedStatement statement = database.prepareStatement("SELECT * FROM " + dbName + ".kpop WHERE name LIKE ? OR id= ? LIMIT 1");
                statement.setString(1, "%"+StringUtils.join(args, " ")+"%");
                statement.setString(2, StringUtils.join(args, " "));

                ResultSet res = statement.executeQuery();

                while (res.next()) {
                    id = res.getString("id");
                    name = res.getString("name");
                    group = res.getString("band");
                    imgUrl = res.getString("img");
                }

            } else {

                Statement statement = database.createStatement();

                ResultSet res = statement.executeQuery("SELECT * FROM " + dbName + ".kpop ORDER BY RAND() LIMIT 1");

                while (res.next()) {
                    id = res.getString("id");
                    name = res.getString("name");
                    group = res.getString("band");
                    imgUrl = res.getString("img");
                }
            }

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
        return "Gives you a random kpop member, command idea by Exa\nUsage: " + Config.prefix + getName() +" [search term]";
    }

    @Override
    public String getName() {
        return "kpop";
    }
}
