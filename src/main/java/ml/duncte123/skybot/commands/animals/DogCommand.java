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

public class DogCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        String base = "https://random.dog/";
        try {
            String jsonString = WebUtils.getText(base + "woof");
            String finalS = base + jsonString;

            if (finalS.contains(".mp4")) {
               sendEmbed(event, EmbedUtils.embedField("A video", "[OMG LOOK AT THIS CUTE VIDEO](" + finalS + ")"));
            } else {
                sendEmbed(event, EmbedUtils.embedImage(finalS));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("**[OOPS]** Something broke, blame duncte"));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "here is a dog.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "dog";
    }
}
