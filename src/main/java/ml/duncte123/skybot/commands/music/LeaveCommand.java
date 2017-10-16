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

package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class LeaveCommand extends MusicCommand {

    public final static String help = "make the bot leave your channel.";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        AudioManager manager = getAudioManager(event.getGuild());

        if(manager.isConnected()){
            if(!manager.getConnectedChannel().getMembers().contains(event.getMember())){
                sendMsg(event,"I'm sorry, but you have to be in the same channel as me to use any music related commands");
                return;
            }

        }else{
            sendMsg(event,"I'm not in a channel atm");
            return;
        }

        if(manager.isConnected()) {
            getMusicManager(event.getGuild()).player.stopTrack();
            manager.setSendingHandler(null);
            manager.closeAudioConnection();
           sendEmbed(event, EmbedUtils.embedField(AirUtils.audioUtils.embedTitle, "Leaving your channel"));
        } else {
            sendMsg(event, "I'm not connected to any channels.");
        }


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
        return "leave";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"disconnect"};
    }
}
