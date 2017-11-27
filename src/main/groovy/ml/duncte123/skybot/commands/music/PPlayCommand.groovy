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
 */

package ml.duncte123.skybot.commands.music

import ml.duncte123.skybot.audio.GuildMusicManager
import ml.duncte123.skybot.objects.command.MusicCommand
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class PPlayCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (channelChecks(event)) {
            Guild guild = event.guild
            GuildMusicManager musicManager = getMusicManager(guild)

            if (args?.length < 1) {
                sendMsg(event, "To few arguments, use `${Settings.prefix}$name <media link>`")
                return
            }

            String toPlay = StringUtils.join(args, " ")
            if (!AirUtils.isURL(toPlay)) {
                toPlay = "ytsearch: " + toPlay
            }

            getAu().loadAndPlay(musicManager, event.channel, toPlay, true)
        }
    }

    @Override
    String help() {
        return "Add a playlist to the queue."
    }

    @Override
    String getName() {
        return "pplay"
    }
}
