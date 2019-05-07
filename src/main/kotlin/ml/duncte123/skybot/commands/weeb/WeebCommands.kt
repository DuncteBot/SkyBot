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

package ml.duncte123.skybot.commands.weeb

import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.weebJava.types.HiddenMode
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.MessageBuilder

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WeebCommands : WeebCommandBase() {

    val weebTags = java.util.ArrayList<String>()

    init {
        this.category = CommandCategory.WEEB
    }

    override fun executeCommand(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        when (ctx.invoke) {
            "hug" -> requestAndSend("hug", "hugs", args, event, ctx.weebApi)
            "lewd" -> sendEmbed(event,
                getWeebEmbedImage(ctx.weebApi.getRandomImage("lewd").execute().url))
            "pat" -> requestAndSend("pat", "pats", args, event, ctx.weebApi)
            "punch" -> requestAndSend("punch", "punches", args, event, ctx.weebApi)
            "shrug" -> sendEmbed(event, getWeebEmbedImageAndDesc("${event.member.effectiveName} shrugs",
                ctx.weebApi.getRandomImage("shrug").execute().url))
            "lick" -> requestAndSend("lick", "licks", args, event, ctx.weebApi)
            "owo" -> sendEmbed(event, getWeebEmbedImage(ctx.weebApi.getRandomImage("owo").execute().url))
            "b1nzy" -> sendEmbed(event, getWeebEmbedImage(ctx.weebApi.getRandomImage(listOf("b1nzy")).execute().url))
            "megumin" -> {
                val quote = ctx.apis.getMeguminQuote()
                val img = ctx.weebApi.getRandomImage("megumin")

                sendEmbed(event, getWeebEmbedImageAndDesc(quote, img.execute().url))
            }
            "weeb" -> {
                if (args.isEmpty()) {
                    sendMsg(event, "Please supply a valid category, Use `${Settings.PREFIX}weeb categories` for all categories")
                    return
                }
                if (weebTags.isEmpty()) {
                    weebTags.addAll(ctx.weebApi.getTypes(HiddenMode.DEFAULT).execute().types)
                }
                if (args[0] == "categories") {
                    sendMsg(event, MessageBuilder()
                        .append("Here is a list of all the valid categories")
                        .appendCodeBlock(weebTags.joinToString(), "LDIF")
                        .build())
                    return
                }
                val type = args.joinToString("")
                if (weebTags.contains(type)) {
                    val img = ctx.weebApi.getRandomImage(type).execute()
                    sendEmbed(event, getWeebEmbedImageAndDesc("Image ID: ${img.id}", img.url))
                } else {
                    sendMsg(event, "That category could not be found, Use `${Settings.PREFIX}weeb_image categories` for all categories")
                }
            }
        }
    }


    override fun help() = """`${Settings.PREFIX}hug` => Hug a user
        |`${Settings.PREFIX}lewd` => When things get to lewd
        |`${Settings.PREFIX}pat` => Pat a user
        |`${Settings.PREFIX}punch` => Punch a user in their face
        |`${Settings.PREFIX}shrug` => ¯\_(ツ)_/¯
        |`${Settings.PREFIX}lick` => Lick a user
        |`${Settings.PREFIX}owo` => OwO what's this
        |`${Settings.PREFIX}weeb <category>` => Gives you a random image from weeb.sh with that type
    """.trimMargin()

    override fun help(invoke: String?): String {
        return when (invoke) {
            "hug" -> {
                """Hug a user.
                    |Usage: `${Settings.PREFIX}$invoke [username/@user]`
                """.trimMargin()
            }
            "lewd" -> {
                """ehhhhh
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "pat" -> {
                """Pats a user.
                    |Usage `${Settings.PREFIX}$invoke [username/@user]`
                """.trimMargin()
            }
            "punch" -> {
                """Punch a user in their face
                    |Usage: `${Settings.PREFIX}$invoke [username/@user]`
                """.trimMargin()
            }
            "shrug" -> {
                """¯\_(ツ)_/¯
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "lick" -> {
                """Lick a user
                    |Usage: `${Settings.PREFIX}$invoke [username/@user]`
                """.trimMargin()
            }
            "owo" -> {
                """OwO what's this
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "megumin" -> {
                """EXPLISION!!!!!
                    |Usage: `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "weeb" -> {
                """Gives you a random image from weeb.sh with that type
                    |Usage: `${Settings.PREFIX}$invoke <category>`
                """.trimMargin()
            }
            else -> {
                "Invoke `$invoke` not reconsigned"
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
        "weeb",
        "b1nzy",
        "megumin"
    )
}
