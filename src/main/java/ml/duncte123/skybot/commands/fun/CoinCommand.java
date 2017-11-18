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
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CoinCommand extends Command {

    public final static String help = "flips a coin.\nUsage: `" + Settings.prefix + "coin`";
    /**
     * these are our images
     */
    private final String[] imagesArr = {"heads.png", "tails.png"};
    public CoinCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        String coinUrl = "https://dshelmondgames.ml/img/coin/";

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("*Flips a coin*").queue();
        MessageEmbed eb = EmbedUtils.embedImage(coinUrl + imagesArr[AirUtils.rand.nextInt(2)]);
        sendEmbed(event, eb);
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public String getName() {
        return "coin";
    }
}
