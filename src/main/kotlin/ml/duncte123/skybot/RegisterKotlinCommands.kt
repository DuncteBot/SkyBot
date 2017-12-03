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

@file:Author

package ml.duncte123.skybot

import ml.duncte123.skybot.commands.`fun`.BlobCommand
import ml.duncte123.skybot.commands.animals.BirbCommand
import ml.duncte123.skybot.commands.essentials.RestartCommand
import ml.duncte123.skybot.commands.essentials.UpdateCommand
import ml.duncte123.skybot.commands.uncategorized.OneLinerCommands
import ml.duncte123.skybot.utils.AirUtils
import org.slf4j.event.Level

@SinceSkybot("3.50.4")
@Author
class RegisterKotlinCommands {
    val manager: CommandManager = AirUtils.commandManager
    
    init {
        AirUtils.log("KotlinCommandManager", Level.INFO, "Registering kotlin commands")
        manager.addCommand(OneLinerCommands())
        manager.addCommand(BlobCommand())
        manager.addCommand(RestartCommand())
        manager.addCommand(BirbCommand())
        manager.addCommand(UpdateCommand())
    }
    
}