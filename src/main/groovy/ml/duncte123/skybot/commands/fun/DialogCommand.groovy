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
 */

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.WordUtils

class DialogCommand extends Command {

    DialogCommand() {
        this.category = CommandCategory.FUN
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            sendMsg(event, "Correct usage: `$PREFIX$name <words>`")
            return
        }

        String[] lines = WordUtils.wrap(
                StringUtils.join(args, " ").replaceAll("`", "")
                , 25, null, true).split("\n")

        StringBuilder sb = new StringBuilder()
                .append("```")
                .append("╔═══════════════════════════╗ \n")
                .append("║ Alert                     ║\n")
                .append("╠═══════════════════════════╣\n")

        Arrays.stream(lines).map{it.trim()}.map{String.format("%-25s", it)}.map{"║ " + it + " ║\n"}.forEach{sb.append(it)}

              sb.append("║  ┌─────────┐  ┌────────┐  ║\n")
                .append("║  │   Yes   │  │   No   │  ║\n")
                .append("║  └─────────┘  └────────┘  ║\n")
                .append("╚═══════════════════════════╝\n")
                .append("```")
        sendEmbed(event, EmbedUtils.embedMessage(sb.toString()))
    }

    @Override
    String help() {
        return "Gives you a nice dialog\n" +
                "Usage: `$PREFIX$name <text>`"
    }

    @Override
    String getName() {
        return "dialog"
    }
}
