/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.audio.TrackScheduler
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class PlayCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (channelChecks(event)) {
            Guild guild = event.guild
            GuildMusicManager mng = getMusicManager(guild)
            AudioPlayer player = mng.player
            TrackScheduler scheduler = mng.scheduler

            if (args?.length == 0) {
                if (player.paused) {
                    player.setPaused(false)
                    sendMsg(event, "Playback has been resumed.")
                } else if (player.playingTrack != null) {
                    sendMsg(event, "Player is already playing!")
                } else if (scheduler.queue.empty) {
                    sendMsg(event, "The current audio queue is empty! Add something to the queue first!")
                }
            } else {
                String toPlay = StringUtils.join(args, " ")
                if (!AirUtils.isURL(toPlay)) {
                    toPlay = "ytsearch: " + toPlay
                }

                if(toPlay.size() > 1024) {
                    sendError(event.message)
                    sendMsg(event, "Input cannot be longer than 1024 characters.")
                    return
                }

                au.loadAndPlay(mng, event.channel, toPlay, false)
            }
        }
    }

    @Override
    String help() {
        return "Make the bot play song.\n" +
                "Usage: `$PREFIX$name [url/search term]`"
    }

    @Override
    String getName() {
        return "play"
    }
}
