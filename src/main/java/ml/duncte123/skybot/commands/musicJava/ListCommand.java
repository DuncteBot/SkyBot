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

package ml.duncte123.skybot.commands.musicJava;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Queue;

public class ListCommand extends MusicCommand {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager mng = getMusicManager(guild);
        TrackScheduler scheduler = mng.scheduler;

        Queue<AudioTrack> queue = scheduler.queue;
        synchronized (queue) {
            if (queue.isEmpty()) {
                sendEmbed(event, EmbedUtils.embedField(getAu().embedTitle, "The queue is currently empty!"));
            } else {
                int trackCount = 0;
                long queueLength = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("Current Queue: Entries: ").append(queue.size()).append("\n");
                for (AudioTrack track : queue) {
                    queueLength += track.getDuration();
                    if (trackCount < 10) {
                        sb.append("`[").append(AudioUtils.getTimestamp(track.getDuration())).append("]` ");
                        sb.append(track.getInfo().title).append("\n");
                        trackCount++;
                    }
                }
                sb.append("\n").append("Total Queue Time Length: ").append(AudioUtils.getTimestamp(queueLength));
               sendEmbed(event, EmbedUtils.embedField(getAu().embedTitle, sb.toString()));
            }
        }
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "shows the current queue";
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"queue"};
    }
}
