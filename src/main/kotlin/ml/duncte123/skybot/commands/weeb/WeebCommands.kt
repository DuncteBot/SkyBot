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
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.MessageBuilder

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WeebCommands : WeebCommandBase() {
    private val weebTags = ArrayList<String>()

    init {
        this.displayAliasesInHelp = true;
        this.category = CommandCategory.WEEB
        this.name = "hug"
        this.aliases = arrayOf(
            "lewd",
            "pat",
            "punch",
            "shoot",
            "shrug",
            "dance",
            "lick",
            "owo",
            "weeb",
            "b1nzy",
            "megumin"
        )
        this.helpFunction = { _, invoke -> this.parseHelp(invoke) }
        this.usageInstructions = { prefix, invoke -> this.parseUsageInstructions(invoke, prefix) }
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        when (ctx.invoke) {
            "hug" -> requestAndSend("hug", "hugs", args, event, ctx.weebApi)
            "lewd" -> singleAction("lewd", "is being lewd", args, event, ctx.weebApi)
            "dance" -> singleAction("dance", "is dancing", args, event, ctx.weebApi)
            "pat" -> requestAndSend("pat", "pats", args, event, ctx.weebApi)
            "punch" -> requestAndSend("punch", "punches", args, event, ctx.weebApi)
            "shoot" -> requestAndSend("bang", "shoots", args, event, ctx.weebApi)
            "shrug" -> sendEmbed(event, getWeebEmbedImageAndDesc("${event.member!!.effectiveName} shrugs",
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
                    sendMsg(event, "Please supply a valid category, Use `${ctx.prefix}weeb categories` for all categories")
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
                    sendMsg(event, "That category could not be found, Use `${ctx.prefix}weeb categories` for all categories")
                }
            }
        }
    }

    private fun basicUsage(invoke: String, prefix: String) = "`$prefix$invoke`"
    private fun userUsage(invoke: String, prefix: String) = "`$prefix$invoke [@user]`"

    private fun parseUsageInstructions(invoke: String, prefix: String): String {
        return when (invoke) {
            "hug" -> this.userUsage(invoke, prefix)
            "lewd" -> this.userUsage(invoke, prefix)
            "pat" -> this.userUsage(invoke, prefix)
            "punch" -> this.userUsage(invoke, prefix)
            "shoot" -> this.userUsage(invoke, prefix)
            "dance" -> this.userUsage(invoke, prefix)
            "shrug" -> this.basicUsage(invoke, prefix)
            "lick" -> this.userUsage(invoke, prefix)
            "owo" -> this.basicUsage(invoke, prefix)
            "megumin" -> this.basicUsage(invoke, prefix)
            "weeb" -> "`$prefix$invoke <category>`"
            "b1nzy" -> this.basicUsage(invoke, prefix)
            else ->  throw IllegalArgumentException("Invalid invoke provided ($invoke)")
        }
    }

    private fun parseHelp(invoke: String): String {
        return when (invoke) {
            "hug" -> "Hug a user"
            "lewd" -> "Someone's being a bit lewd"
            "pat" -> "Pat someone"
            "punch" -> "Punch someone in their face"
            "shoot" -> "Shoot someone"
            "dance" -> "Do a little dance"
            "shrug" -> "¯\\\\_(ツ)\\_/¯"
            "lick" -> "Lick someone"
            "owo" -> "OwO what's this"
            "megumin" -> "EXPLOSION!!!!!"
            "weeb" -> "Gives you a random image from weeb.sh with that type"
            "b1nzy" -> "Shows a b1nzy meme"
            else ->  throw IllegalArgumentException("Invalid invoke provided ($invoke)")
        }
    }
}
