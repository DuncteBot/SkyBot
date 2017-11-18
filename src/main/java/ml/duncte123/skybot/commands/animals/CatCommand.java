/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

import java.net.URL;

public class CatCommand extends Command {

    public final static String help = "here is a cat.";

    public CatCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        try {
            String jsonString = WebUtils.getText("http://random.cat/meow");
            JSONObject jsonObject = new JSONObject(jsonString);
            String newJSON = jsonObject.getString("file");
            event.getChannel().sendFile(new URL(newJSON).openStream(), "cat_" + System.currentTimeMillis() + ".png", null).queue();
        } catch (Exception e) {
            e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("OOPS: " + e.getMessage()));
        }

    }

    @Override
    public String help() {
        return help;
    }

    @Override
    public String getName() {
        return "cat";
    }
}
