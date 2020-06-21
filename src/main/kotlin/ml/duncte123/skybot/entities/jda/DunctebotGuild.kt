/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.guild.GuildSettings
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.api.entities.Guild

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DunctebotGuild(private val guild: Guild, private val variables: Variables) : Guild by guild {

    fun getSettings() = GuildSettingsUtils.getGuild(this.idLong, this.variables)

    fun setSettings(settings: GuildSettings) {
        GuildSettingsUtils.updateGuildSettings(this.idLong, settings, this.variables)
    }

    fun setColor(color: Int) {
        EmbedUtils.addColor(this.idLong, color)
        GuildSettingsUtils.updateEmbedColor(this.idLong, color, this.variables)
    }

    fun getColor(): Int {
        return EmbedUtils.getColorOrDefault(this.idLong, Settings.DEFAULT_COLOUR)
    }

    fun getHexColor(): String {
        return AirUtils.colorToHex(getColor())
    }

    override fun toString() = this.guild.toString()
}
