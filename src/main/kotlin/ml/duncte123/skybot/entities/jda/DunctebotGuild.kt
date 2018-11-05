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

package ml.duncte123.skybot.entities.jda

import me.duncte123.botcommons.messaging.EmbedUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.entities.Guild

@Author(nickname = "duncte123", author = "Duncan Sterken")
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

    fun setColor(color: Int) {
        EmbedUtils.addColor(idLong, color)
        GuildSettingsUtils.updateEmbedColor(this.guild, color, variables)
    }

    private fun getColor(): Int {
        return EmbedUtils.getColor(idLong)
    }

    fun getHexColor(): String {
        return AirUtils.colorToHex(getColor())
    }

    override fun toString() = "G:${this.guild.name} (${this.guild.id}"
}
