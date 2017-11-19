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
import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.EmbedUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class NowPlayingCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        GuildMusicManager mng = getMusicManager(event.guild)
        AudioPlayer player = mng.player
        def msg
        if (player.playingTrack != null) {
            msg = "**Playing** " + player.playingTrack.info.title + "\n" + EmbedUtils.playerEmbed(mng)
        } else {
            msg = "The player is not currently playing anything!"
        }
        sendEmbed(event, EmbedUtils.embedMessage(msg))
    }

    @Override
    String help() {
        return "Prints information about the currently playing song (title, current time)"
    }

    @Override
    String getName() {
        return "nowplaying"
    }

    @Override
    String[] getAliases() {
        return ["np", "song"]
    }
}
