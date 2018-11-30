/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.adapters

import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.guild.GuildSettings

@Suppress("UNUSED_PARAMETER")
abstract class DatabaseAdapter(variables: Variables) {

    //////////////////
    // Custom commands

    abstract fun getCustomCommands(callback: (List<CustomCommand>) -> Unit)

    /**
     * Creates a custom command
     *
     * @param guildId
     *          the id of the guild
     *
     * @param invoke
     *          the invoke of the command
     *
     * @param message
     *          the action of the command
     *
     * @param callback
     *          the result of the action
     *          the boolean values in the tripple are:
     *             1. True when the command was added
     *             2. True when the guild already has a command with this invoke
     *             3. True when the guild reached the custom command limit
     */
    abstract fun createCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit)

    abstract fun updateCustomCommand(guildId: Long, invoke: String, message: String, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit)

    abstract fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit)

    /////////////////
    // Guild settings

    abstract fun getGuildSettings(callback: (List<GuildSettings>) -> Unit)

}
