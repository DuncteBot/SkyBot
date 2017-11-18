/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class PauseCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (channelChecks(event)) {
            AudioPlayer player = getMusicManager(event.guild).player

            if (player.playingTrack == null) {
                sendMsg(event, "Cannot pause or resume player because no track is loaded for playing.")
                return
            }

            player.setPaused(!player.paused)
            if (player.paused) {
                sendMsg(event, "The player has been paused.")
            } else {
                sendMsg(event, "The player has resumed playing.")
            }
        }
    }

    @Override
    String help() {
        return "Pauses the current song"
    }

    @Override
    String getName() {
        return "pause"
    }
}
