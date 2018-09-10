/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.entities.jda

import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.entities.Guild

class DunctebotGuild(private val guild: Guild, private val variables: Variables) : Guild by guild {

    /**
     * Gets the settings for a guild
     */
    fun getSettings() = GuildSettingsUtils.getGuild(this.guild, variables)

    /**
     * Sets the settings for a guild
     */
    fun setSettings(settings: GuildSettings) {
        GuildSettingsUtils.updateGuildSettings(this.guild, settings, variables)
    }

    override fun toString() = "G:${this.guild.name} (${this.guild.id}"
}