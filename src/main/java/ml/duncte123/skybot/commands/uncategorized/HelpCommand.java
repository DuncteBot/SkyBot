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
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.HelpEmbeds;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

public class HelpCommand extends Command {

    public final static String help = "Shows a list of all the commands.\nUsage: `" + Settings.prefix + "help [command]`";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        if(args.length > 0) {
            String toSearch = StringUtils.join(args, " ");

            for(Command cmd : AirUtils.commandSetup.getCommands()) {
                if(cmd.getName().equals(toSearch)) {
                    sendMsg(event, "Command help for `"+ cmd.getName()+"` :\n" + cmd.help() + (cmd.getAliases().length > 0 ? "\nAliases: " + StringUtils.join(cmd.getAliases(), ", ") : "") );
                    return;
                } else {

                    for(String alias : cmd.getAliases()) {

                        if(alias.equals(toSearch)) {
                            sendMsg(event, "Command help for `"+cmd.getName()+"` :\n" + cmd.help() + (cmd.getAliases().length > 0 ? "\nAliases: " + StringUtils.join(cmd.getAliases(), ", ") : "") );
                            return;
                        }

                    }

                }
            }

            sendMsg(event, "That command could not be found, try "+ Settings.prefix+"help for a list of commands");
            return;
        }

        event.getAuthor().openPrivateChannel().queue(
            pc -> pc.sendMessage(HelpEmbeds.getCommandListWithPrefix(getSettings(event.getGuild().getId()).getCustomPrefix() ) ).queue(
                 msg ->  event.getChannel().sendMessage(event.getMember().getAsMention() +" check your DM's").queue(),
                //When sending fails, send to the channel
                err -> event.getChannel().sendMessage(HelpEmbeds.commandList).complete().getChannel().sendMessage("Message could not be delivered to dm's and has been send in this channel.").queue()
            ),
            err -> event.getChannel().sendMessage("ERROR: " + err.getMessage()).queue()
        );
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public String getName() {
        return "help";
    }
}
