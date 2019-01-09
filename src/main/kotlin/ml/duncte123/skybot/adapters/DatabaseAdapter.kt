/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import gnu.trove.map.TLongIntMap
import gnu.trove.map.TLongLongMap
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.api.Ban
import ml.duncte123.skybot.objects.api.Mute
import ml.duncte123.skybot.objects.api.Warning
import ml.duncte123.skybot.objects.command.custom.CustomCommand
import ml.duncte123.skybot.objects.guild.GuildSettings
import java.util.*

@Author(nickname = "duncte123", author = "Duncan Sterken")
abstract class DatabaseAdapter(@Suppress("UNUSED_PARAMETER") protected val variables: Variables) {

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

    abstract fun updateCustomCommand(guildId: Long, invoke: String, message: String, autoresponse: Boolean, callback: (Triple<Boolean, Boolean, Boolean>?) -> Unit)

    abstract fun deleteCustomCommand(guildId: Long, invoke: String, callback: (Boolean) -> Unit)

    /////////////////
    // Guild settings

    abstract fun getGuildSettings(callback: (List<GuildSettings>) -> Unit)

    abstract fun loadGuildSetting(guildId: Long, callback: (GuildSettings) -> Unit)

    abstract fun updateGuildSetting(guildSettings: GuildSettings, callback: (Boolean) -> Unit)

    abstract fun registerNewGuild(guildSettings: GuildSettings, callback: (Boolean) -> Unit)

    /////////////////
    // Embed settings

    abstract fun loadEmbedSettings(callback: (TLongIntMap) -> Unit)

    abstract fun updateOrCreateEmbedColor(guildId: Long, color: Int)

    ///////////////
    // Patron stuff

    abstract fun loadOneGuildPatrons(callback: (TLongLongMap) -> Unit)

    abstract fun addOneGuildPatrons(userId: Long, guildId: Long, callback: (Long, Long) -> Unit)

    abstract fun getOneGuildPatron(userId: Long, callback: (TLongLongMap) -> Unit)

    abstract fun removeOneGuildPatron(userId: Long)

    /////////////
    // Moderation

    abstract fun createBan(modId: Long, userName: String, userDiscriminator: String, userId: Long, unbanDate: String, guildId: Long)

    abstract fun createWarning(modId: Long, userId: Long, guildId: Long, reason: String)

    abstract fun createMute(modId: Long, userId: Long, userTag: String, unmuteDate: String, guildId: Long)

    abstract fun getWarningsForUser(userId: Long, guildId: Long, callback: (List<Warning>) -> Unit)

    abstract fun getExpiredBansAndMutes(callback: (Pair<List<Ban>, List<Mute>>) -> Unit)

    abstract fun purgeBans(ids: List<Int>)

    abstract fun purgeMutes(ids: List<Int>)
}
