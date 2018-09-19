/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

public class DogCommand extends Command {

    public DogCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        String base = "https://random.dog/";
        GuildMessageReceivedEvent event = ctx.getEvent();
        try {
            WebUtils.ins.getText(base + "woof").async(it -> {
                String finalS = base + it;

                if (finalS.contains(".mp4")) {
                    sendEmbed(event, EmbedUtils.embedField("A video", "[Click for video](" + finalS + ")"));
                } else {
                    sendEmbed(event, EmbedUtils.embedImage(finalS));
                }
            });

        } catch (Exception e) {
            //e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("**[OOPS]** Something broke, blame duncte \n(" + e.toString() + ")"));
            ComparatingUtils.execCheck(e);
        }

    }

    @Override
    public String help() {
        return "here is a dog.";
    }

    @Override
    public String getName() {
        return "dog";
    }
}
