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
 *
 */

package ml.duncte123.skybot.commands.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AudioUtils
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class ListCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(channelChecks(event)) {
            def scheduler = getMusicManager(event.guild).scheduler

            Queue<AudioTrack> queue = scheduler.queue
            synchronized (queue) {
                if (queue.empty) {
                    sendEmbed(event, EmbedUtils.embedField(au.embedTitle, "The queue is currently empty!"))
                } else {
                    int trackCount = 0
                    long queueLength = 0
                    StringBuilder sb = new StringBuilder()
                    sb.append("Current Queue: Entries: ").append(queue.size()).append("\n")
                    for (AudioTrack track : queue) {
                        queueLength += track.duration
                        if (trackCount < 10) {
                            sb.append("`[").append(AudioUtils.getTimestamp(track.duration)).append("]` ")
                            sb.append(track.info.title).append("\n")
                            trackCount++
                        }
                    }
                    sb.append("\n").append("Total Queue Time Length: ").append(AudioUtils.getTimestamp(queueLength))
                    sendEmbed(event, EmbedUtils.embedField(au.embedTitle, sb.toString()))
                }
            }
        }
    }

    @Override
    String help() {
        return "shows the current queue"
    }

    @Override
    String getName() {
        return "list"
    }

    @Override
    String[] getAliases() {
        return ["queue"]
    }
}
