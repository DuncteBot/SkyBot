/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class SealCommand extends Command {

    public SealCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        int availableSeals = 83;
        int sealID = (int) Math.floor(Math.random() * availableSeals) + 1;
        String idStr = ("0000" + String.valueOf(sealID)).substring(String.valueOf(sealID).length());
        String sealLoc = "https://raw.githubusercontent.com/TheBITLINK/randomse.al/master/seals/" + idStr + ".jpg";
        MessageUtils.sendEmbed(event, EmbedUtils.embedImage(sealLoc));

    }

    @Override
    public String help() {
        return "Here is a nice seal";
    }

    @Override
    public String getName() {
        return "seal";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"zeehond"};
    }
}
