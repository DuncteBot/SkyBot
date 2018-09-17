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

package ml.duncte123.skybot.commands.nsfw

import com.afollestad.ason.Ason
import me.duncte123.botCommons.messaging.MessageUtils
import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed

class NSFWCommands : Command() {

    private val nekkobotBase = "https://nekobot.xyz/api/image?type="

    init {
        this.category = CommandCategory.NSFW
        this.displayAliasesInHelp = true
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!event.channel.isNSFW) {
            MessageUtils.sendMsg(event, """Woops, this channel is not marked as NSFW.
                |Please mark this channel as NSFW to use this command
                """.trimMargin())
            return
        }
        when (ctx.invoke) {
            "carsandhentai" -> {
                WebUtils.ins.getAson(String.format(ctx.googleBaseUrl, "Cars and hentai")).async { jsonRaw ->
                    val jsonArray = jsonRaw.getJsonArray<Ason>("items")
                    val randomItem = jsonArray.getJsonObject(ctx.random.nextInt(jsonArray.size()))
                    sendEmbed(event,
                            EmbedUtils.defaultEmbed()
                                    .setTitle(randomItem?.getString("title"), randomItem?.getString("image.contextLink"))
                                    .setImage(randomItem?.getString("link")).build()
                    )
                }

            }
            "lewdneko" -> {
                WebUtils.ins.getJSONObject("https://nekos.life/api/v2/img/lewd").async {
                    sendEmbed(event, EmbedUtils.embedImage(it.getString("url")))
                }
            }
            "lewdkitsune" -> {
                WebUtils.ins.getJSONObject("${nekkobotBase}lewdkitsune").async {
                    sendEmbed(event, EmbedUtils.embedImage(it.getString("message")))
                }
            }
            "hentai" -> {
                val t = if (ctx.random.nextInt(2) == 1) "hentai" else "hentai_anal"
                WebUtils.ins.getJSONObject("$nekkobotBase$t").async {
                    sendEmbed(event, EmbedUtils.embedImage(it.getString("message")))
                }
            }
        }
    }

    override fun help() = """`${PREFIX}lewdneko` => Gives a very lewd neko
        |`${PREFIX}carsandhentai` => Delet this
        |`${PREFIX}lewdkitsune` => Shows you a lewd kitsune
        |`${PREFIX}hentai` => Just some hentai
    """.trimMargin()

    override fun help(invoke: String?): String {
        return when (invoke) {
            "lewdneko" -> {
                """Gives a very lewd neko
                    |Usage `$PREFIX$invoke`
                """.trimMargin()
            }
            "carsandhentai" -> {
                """Delet this
                    |Usage `$PREFIX$invoke`
                """.trimMargin()
            }
            "lewdkitsune" -> {
                """Shows you a lewd kitsune
                    |Usage `$PREFIX$invoke`
                """.trimMargin()
            }
            "hentai" -> {
                """Just some hentai
                    |Usage `$PREFIX$invoke`
                """.trimMargin()
            }
            else -> "No U"
        }
    }

    override fun getName() = "lewdneko"

    override fun getAliases() = arrayOf("carsandhentai", "lewdkitsune", "hentai")
}