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

package ml.duncte123.skybot

import ml.duncte123.skybot.commands.fun.TextToBricksCommand
import ml.duncte123.skybot.commands.music.*
import ml.duncte123.skybot.commands.uncategorized.ShortenCommand
import ml.duncte123.skybot.utils.AirUtils
import org.slf4j.event.Level

class RegisterGroovyCommands {
    def manager = AirUtils.commandManager

    RegisterGroovyCommands() {
        AirUtils.log("GroovyCommandManager", Level.INFO, "Registering groovy commands")

        manager.addCommand(new ShortenCommand())
        manager.addCommand(new TextToBricksCommand())

        //Add the music commands
        manager.addCommand(new JoinCommand())
        manager.addCommand(new LeaveCommand())
        manager.addCommand(new ListCommand())
        manager.addCommand(new NowPlayingCommand())
        manager.addCommand(new PauseCommand())
        manager.addCommand(new PlayCommand())
        manager.addCommand(new PlayRawCommand())
        manager.addCommand(new PPlayCommand())
        manager.addCommand(new RepeatCommand())
        manager.addCommand(new ShuffleCommand())
        manager.addCommand(new SkipCommand())
        manager.addCommand(new StopCommand())
    }

}
