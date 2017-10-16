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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class NowPlayingCommand extends MusicCommand {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager mng = getMusicManager(guild);
        AudioPlayer player = mng.player;

        String msg = "";

        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != null){
            String title = currentTrack.getInfo().title;
            String position = AudioUtils.getTimestamp(currentTrack.getPosition());
            String duration = AudioUtils.getTimestamp(currentTrack.getDuration());

            msg = String.format("**Playing:** %s\n**Time:** [%s / %s]",
                    title, position, duration);
            msg = "**Playing** " + title + "\n" + EmbedUtils.playerEmbed(mng);
        }else{
            msg = "The player is not currently playing anything!";
        }
        sendEmbed(event, EmbedUtils.embedMessage(msg));

    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Prints information about the currently playing song (title, current time)";
    }

    @Override
    public String getName() {
        return "nowplaying";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"np"};
    }

}
