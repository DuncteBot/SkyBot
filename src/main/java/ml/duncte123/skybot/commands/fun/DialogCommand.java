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

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;

public class DialogCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if (args.length < 1) {
            event.getChannel().sendMessage("Correct usage: `"+ Settings.prefix+getName()+" <words>`").queue();
            return;
        }

        String linesBeforeWrap = StringUtils.join(args, " ").replaceAll("`", "");

        String lines = WordUtils.wrap(linesBeforeWrap, 25, null, true);

        String[] split = lines.split("\n");

        StringBuilder sb = new StringBuilder()
                .append("```")
                .append("╔═══════════════════════════╗ \n")
                .append("║ Alert                     ║\n")
                .append("╠═══════════════════════════╣\n");
                //.append("║ " + String.format("%-25s", lines) + " ║\n") //Thnx Kantenkugel#1568 you're awesome
                Arrays.stream(split).map(String::trim).map( it -> String.format("%-25s", it) ).map(it -> "║ "+it+" ║\n").forEach(sb::append);
              sb.append("║  ┌─────────┐  ┌────────┐  ║\n")
                .append("║  │   Yes   │  │   No   │  ║\n")
                .append("║  └─────────┘  └────────┘  ║\n")
                .append("╚═══════════════════════════╝\n")
                .append("```");
        sendEmbed(event, EmbedUtils.embedMessage(sb.toString()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Gives you a nice dialog";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "dialog";
    }
}
