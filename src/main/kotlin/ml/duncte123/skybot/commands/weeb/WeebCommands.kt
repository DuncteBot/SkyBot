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

package ml.duncte123.skybot.commands.weeb

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.weebJava.types.HiddenMode
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import net.dv8tion.jda.core.MessageBuilder
import org.apache.commons.lang3.StringUtils

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
                WebUtils.ins.getJSONObject("https://megumin.torque.ink/api/explosion").async({
                    val chant = it.optString("chant")
                    val img = it.optString("img")
                    sendEmbed(event, getWeebEmbedImageAndDesc(chant, img))
                }, {
                    //When the site is down or dies
                    val img = ctx.weebApi.getRandomImage("megumin")
                    sendEmbed(event, getWeebEmbedImage(img.execute().url))
                })
            }
            "weeb" -> {
                if (args.isEmpty()) {
                    sendMsg(event, "Please supply a valid category, Use `${PREFIX}weeb categories` for all categories")
                    return
                }
                if (weebTags.isEmpty()) {
                    weebTags.addAll(ctx.weebApi.getTypes(HiddenMode.DEFAULT).execute().types)
                }
                if (args[0] == "categories") {
                    sendMsg(event, MessageBuilder()
                        .append("Here is a list of all the valid categories")
                        .appendCodeBlock(StringUtils.join(weebTags, ", "), "LDIF")
                        .build())
                    return
                }
                val type = StringUtils.join(args, "")
                if (weebTags.contains(type)) {
                    val img = ctx.weebApi.getRandomImage(StringUtils.join(args, "")).execute()
                    sendEmbed(event, getWeebEmbedImageAndDesc("Image ID: ${img.id}", img.url))
                } else {
                    sendMsg(event, "That category could not be found, Use `${PREFIX}weeb_image categories` for all categories")
                }
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
        |`${PREFIX}weeb <category>` => Gives you a random image from weeb.sh with that type
    """.trimMargin()

    override fun help(invoke: String?): String {
        return when (invoke) {
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
            "megumin" -> {
                """EXPLISION!!!!!
                    |Usage: `$PREFIX$invoke`
                """.trimMargin()
            }
            "weeb" -> {
                """Gives you a random image from weeb.sh with that type
                    |Usage: `$PREFIX$invoke <category>`
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
