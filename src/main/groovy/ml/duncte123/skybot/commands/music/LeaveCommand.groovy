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

import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class LeaveCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (channelChecks(event)) {
            def manager = getAudioManager(event.guild)

            if (manager.connected) {
                getMusicManager(event.getGuild()).player.stopTrack()
                manager.setSendingHandler(null)
                manager.closeAudioConnection()
                sendMsg(event, "Leaving your channel")
            } else {
                sendMsg(event, "I'm not connected to any channels.")
            }
        }
    }

    @Override
    String help() {
        return "Makes the bot leave your channel."
    }

    @Override
    String getName() {
        return "leave"
    }

    @Override
    String[] getAliases() {
        return ["disconnect"]
    }
}
