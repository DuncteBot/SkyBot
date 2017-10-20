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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class PingCommand extends Command {

    public final static String help = "Shows the delay from the bot to the discord servers.\nUsage: `"+ Settings.prefix+"ping`";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param invoke
     * @param args The command agruments
     * @param event a instance of {@link MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        long time = System.currentTimeMillis();

        event.getChannel().sendMessage("PONG!").queue( (message) ->
            message.editMessageFormat("PONG!" +
                    "\nping is: %dms " +
                    "\nWebsocket ping: %dms\n" +
                    "Average ping: %dms",
                    (System.currentTimeMillis() - time),
                    event.getJDA().getPing(),
                    event.getJDA().asBot().getShardManager().getAveragePing() ).queue());

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return help;
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"pong"};
    }
}
