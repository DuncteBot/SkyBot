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

import ml.duncte123.skybot.audio.TrackScheduler
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class RepeatCommand extends MusicCommand {
    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (channelChecks(event)) {
            TrackScheduler scheduler = getMusicManager(event.guild).scheduler

            scheduler.setRepeating(!scheduler.repeating)
            sendMsg(event, "Player was set to: **" + (scheduler.repeating ? "" : "not") + "repeat**")
        }
    }

    @Override
    String help() {
        return "Makes the player repeat the currently playing song/"
    }

    @Override
    String getName() {
        return "repeat"
    }

    @Override
    String[] getAliases() {
        return ["loop"]
    }
}
