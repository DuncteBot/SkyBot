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
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class HugCommand : WeebCommandBase() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        val imageUrl = AirUtils.WEEB_API.getRandomImage("hug").url

        if(args.isEmpty()) {
            sendEmbed(event, getWeebEmbedImageAndDesc(
                    "${Settings.defaultName} hugs ${event.member.effectiveName}", imageUrl))
        } else {
            if(!event.message.mentionedMembers.isEmpty()) {
                sendEmbed(event, getWeebEmbedImageAndDesc(
                        "${event.member.effectiveName} hugs ${event.message.mentionedMembers[0].effectiveName}"
                        , imageUrl))
            } else {
                sendEmbed(event, getWeebEmbedImageAndDesc(
                        "${event.member.effectiveName} hugs ${StringUtils.join(args, " ")}", imageUrl))
            }
        }
    }

    override fun help() = """Hug a user.
        |Usage: `$name [username/@user]`
    """.trimMargin()

    override fun getName() = "hug"
}