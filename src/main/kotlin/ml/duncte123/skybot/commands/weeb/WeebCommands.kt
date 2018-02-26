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

import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class WeebCommands : WeebCommandBase() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        when (invoke) {
            "hug" -> thatStuffThatINeedToDoALotOfTimes("hug", "hugs", args, event)
            "lewd" -> sendEmbed(event,
                    getWeebEmbedImage(AirUtils.WEEB_API.getRandomImage("lewd", "${event.channel.isNSFW}").url))
            "pat" -> thatStuffThatINeedToDoALotOfTimes("pat", "pats", args, event)
            "punch" -> thatStuffThatINeedToDoALotOfTimes("punch", "punches", args, event)
            "shrug" -> sendEmbed(event, getWeebEmbedImageAndDesc("${event.member.effectiveName} shrugs",
                    AirUtils.WEEB_API.getRandomImage("shrug").url))
            "lick" -> thatStuffThatINeedToDoALotOfTimes("lick", "licks", args, event)
            "owo" -> sendEmbed(event, getWeebEmbedImage(AirUtils.WEEB_API.getRandomImage("owo").url))
            "weeb_image" -> {
                if(args.isEmpty()) {
                    sendMsg(event, "Please supply a valid category, Use `${PREFIX}weeb_image categories` for all categories")
                    return
                }
                if(args[0] == "categories") {
                    sendMsg(event, MessageBuilder()
                            .append("Here is a list of all the valid categories")
                            .appendCodeBlock(StringUtils.join(AirUtils.WEEB_API.types, ", "), "LDIF")
                            .build())
                    return
                }
                val img =  AirUtils.WEEB_API.getRandomImage(StringUtils.join(args, " "))
                sendEmbed(event, getWeebEmbedImageAndDesc("Image ID: ${img.id}", img.url))
            }
        }
    }


    override fun help() = """`${PREFIX}hug` => Hug a user
        |`${PREFIX}lewd` => When things get to lewd
        |`${PREFIX}pat` => Pat a user
        |`${PREFIX}punch` => Punch a user in their face
        |`${PREFIX}shrug` => ¯\_(ツ)_/¯
        |`${PREFIX}lick` => Lick a user
        |`${PREFIX}owo` => OwO what's this
        |`${PREFIX}weeb_image <category>` => Gives you a random image from weeb.sh with that type
    """.trimMargin()

    override fun help(invoke: String?): String {
         return when(invoke) {
            "hug" -> {
                """Hug a user.
                    |Usage: `$PREFIX$invoke [username/@user]`
                """.trimMargin()
            }
            "lewd" -> {
                """ehhhhh
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "pat" -> {
                """Pats a user.
                    |Usage `$PREFIX$invoke [username/@user]`
                """.trimMargin()
            }
            "punch" -> {
                """Punch a user in their face
                    |Usage: `$PREFIX$invoke [username/@user]`
                """.trimMargin()
            }
            "shrug" -> {
                """¯\_(ツ)_/¯
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "lick" -> {
                """Lick a user
                    |Usage: `$PREFIX$invoke [username/@user]`
                """.trimMargin()
            }
             "owo" -> {
                 """OwO what's this
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
             }
             "weeb_image" -> {
                 """Gives you a random image from weeb.sh with that type
                    |Usage: `$PREFIX$invoke <category>`
                """.trimMargin()
             }
            else -> {
             "wrong invoke"
            }
        }
    }

    override fun getName() = "hug"

    override fun getAliases() = arrayOf(
            "lewd",
            "pat",
            "punch",
            "shrug",
            "lick",
            "owo",
            "weeb_image"
    )
}