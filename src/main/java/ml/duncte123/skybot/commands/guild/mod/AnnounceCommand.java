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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class AnnounceCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        Permission[] perms = {
                Permission.ADMINISTRATOR
        };

        if(!event.getMember().hasPermission(perms)) {
            sendMsg(event, "I'm sorry but you don't have permission to run this command.");
            return;
        }

        if(event.getMessage().getMentionedChannels().size() < 1) {
            sendMsg(event, "Correct usage is `" + Settings.prefix + getName() + " [#Channel] [Message]`");
            return;
        }

        try{

            TextChannel chann = event.getMessage().getMentionedChannels().get(0);
            String msg = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");

            sendEmbed(event, EmbedUtils.embedMessage(msg));

        }
        catch (Exception e) {
            sendMsg(event, "WHOOPS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Announces a message.";
    }

    @Override
    public String getName() {
        return "announce";
    }
}
