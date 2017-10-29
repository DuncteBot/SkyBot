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
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class TagCommand extends Command {
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if(args.length == 1) {

            if(args[0].equals("help") ||  args[0].equals("?")) {
               //TODO: help
            } else if(args[0].equals("who") || args[0].equals("author")) {
                //TODO: who made it
            } else {
                //TODO: select tag
            }

        } else if(args.length > 3 && args[0].equals("create")) {
            //TODO: create tag
        }

    }

    @Override
    public String help() {
        return "Save it in a tag\n" +
                "Usage: `"+this.PREFIX+getName()+" <tag_name/help/create> [tag_name] [tag contents]`";
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"pasta", "tags", "t"};
    }
}
