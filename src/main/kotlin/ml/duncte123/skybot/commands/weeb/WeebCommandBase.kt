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

package ml.duncte123.skybot.commands.weeb

import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

abstract class WeebCommandBase : Command() {
    init {
        this.category = CommandCategory.WEEB
    }

    fun getDefaultWeebEmbed(): EmbedBuilder {
        return EmbedUtils.defaultEmbed()
                .setFooter("Powered by weeb.sh & weeb.java", null)
                .setTimestamp(null)
    }

    fun getWeebEmbedImageAndDesc(description: String, imageUrl: String): MessageEmbed {
        return getDefaultWeebEmbed().setDescription(description).setImage(imageUrl).build()
    }

    fun getWeebEmbedImage(imageUrl: String): MessageEmbed {
        return getDefaultWeebEmbed().setImage(imageUrl).build()
    }

    fun thatStuffThatINeedToDoALotOfTimes(type: String, thing: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        val imageUrl = AirUtils.WEEB_API.getRandomImage(type).url

        if(args.isEmpty()) {
            MessageUtils.sendEmbed(event, getWeebEmbedImageAndDesc(
                    "${Settings.defaultName} $thing ${event.member.effectiveName}", imageUrl))
        } else {
            if(!event.message.mentionedMembers.isEmpty()) {
                MessageUtils.sendEmbed(event, getWeebEmbedImageAndDesc(
                        "${event.member.effectiveName} $thing ${event.message.mentionedMembers[0].effectiveName}"
                        , imageUrl))
            } else {
                MessageUtils.sendEmbed(event, getWeebEmbedImageAndDesc(
                        "${event.member.effectiveName} $thing ${StringUtils.join(args, " ")}", imageUrl))
            }
        }
    }
}