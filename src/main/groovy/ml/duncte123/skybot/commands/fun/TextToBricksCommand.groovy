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
 *
 */

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class TextToBricksCommand extends Command {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            sendMsg(event, "Correct usage: `${Settings.prefix}$invoke <words>`")
            return
        }

        def output = StringUtils.join(args, " ")
                .replaceAll("([a-zA-Z])", ":regional_indicator_\$1:")
                .replaceAll("([0-9])", "\$1\u20E3")
        sendEmbed(event, EmbedUtils.embedMessage(output))
    }

    @Override
    String help() {
        return "Convert your text to bicks"
    }

    @Override
    String getName() {
        return "ttb"
    }
}
