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
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class BirbCommand extends Command {
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        try {
            String imgName = WebUtils.getText("https://proximyst.com:4500/random/path/text");

            sendEmbed(event, EmbedUtils.embedImage("https://proximyst.com:4500/image/" + imgName + "/image"));
        }
        catch (IOException e) {
            sendMsg(event, "ERROR: " + e.getMessage());
        }
    }

    @Override
    public String help() {
        return "Here is a Birb";
    }

    @Override
    public String getName() {
        return "birb";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"bird"};
    }
}
