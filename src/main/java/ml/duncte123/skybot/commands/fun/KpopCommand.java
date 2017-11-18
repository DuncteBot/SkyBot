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
 *
 */

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

public class KpopCommand extends Command {

    public KpopCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        String id = "";
        String name = "";
        String group = "";
        String imgUrl = "";

        String dbName = AirUtils.db.getName();
        Connection database = AirUtils.db.getConnManager().getConnection();
        try {

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

            EmbedBuilder eb = EmbedUtils.defaultEmbed()
                    .setDescription("Here is a kpop member from the group " + group)
                    .addField("Name of the member", name, false)
                    .setImage(imgUrl)
                    .setFooter("Query id: " + id, Settings.defaultIcon);
            sendEmbed(event, eb.build());
        }
        catch (Exception e) {
           sendMsg(event, "SCREAM THIS TO _duncte123#1245_: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                database.close();
            }
            catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Override
    public String help() {
        return "Gives you a random kpop member, command idea by Exa\nUsage: " + Settings.prefix + getName() +" [search term]";
    }

    @Override
    public String getName() {
        return "kpop";
    }
}
