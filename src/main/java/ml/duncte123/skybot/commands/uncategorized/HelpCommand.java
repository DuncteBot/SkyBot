/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
