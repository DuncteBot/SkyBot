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

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class PPlayCommand extends MusicCommand {

    public final static String help = "add a playlist to the queue.";

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param invoke
     * @param args The command agruments
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getMusicManager(guild);
        AudioManager audioManager = getAudioManager(guild);

        if(!audioManager.isConnected()){
            sendMsg(event, "I'm not in a voice channel, use `"+ Settings.prefix+"join` to make me join a channel");
            return;
        }

        if(!audioManager.getConnectedChannel().getMembers().contains(event.getMember())){
            sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return;
        }

        if(args.length < 1){
            sendMsg(event, "To few arguments, use `"+ Settings.prefix+"pplay <media link>`");
            return;
        }

        String toPlay = StringUtils.join(Arrays.asList(args), " ");
        if(!AirUtils.isURL(toPlay)){
            toPlay = "ytsearch: " + toPlay;
        }

        getAu().loadAndPlay(musicManager, event.getChannel(), toPlay, true);

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
        return "pplay";
    }

}
