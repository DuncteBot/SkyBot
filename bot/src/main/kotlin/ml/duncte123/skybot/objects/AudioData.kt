/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects

import ml.duncte123.skybot.Variables
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

data class AudioData(
    val guildId: Long,
    val userId: Long,
    val channelId: Long,
    val replyToMessage: Long,
    val jda: JDA,
    val variables: Variables,
) {
    fun getChannel() = jda.getChannelById(MessageChannelUnion::class.java, channelId)!!

    companion object {
        fun fromSlash(event: SlashCommandInteractionEvent, variables: Variables): AudioData = AudioData(
            event.guild!!.idLong,
            event.user.idLong,
            event.channel.idLong,
            event.hook.retrieveOriginal().complete().idLong,
            event.jda,
            variables
        )
    }
}
