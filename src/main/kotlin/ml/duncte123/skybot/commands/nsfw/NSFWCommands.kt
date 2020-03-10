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

package ml.duncte123.skybot.commands.nsfw

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

@Author(nickname = "duncte123", author = "Duncan Sterken")
class NSFWCommands : Command() {

    private val nekkobotBase = "https://nekobot.xyz/api/image?type="

    init {
        this.displayAliasesInHelp = true
        this.category = CommandCategory.NSFW
        this.name = "lewdneko"
        this.aliases = arrayOf("carsandhentai", "lewdkitsune", "hentai")
        this.helpFunction = { _, invoke -> this.parseCommandHelp(invoke) }
    }

    override fun execute(ctx: CommandContext) {
        when (ctx.invoke.toLowerCase()) {
            "carsandhentai" -> {
                WebUtils.ins.getJSONObject(String.format(ctx.googleBaseUrl, "Cars and hentai")).async { jsonRaw ->
                    val jsonArray = jsonRaw["items"]
                    val randomItem = jsonArray[ctx.random.nextInt(jsonArray.size())]
                    sendEmbed(ctx,
                        EmbedUtils.defaultEmbed()
                            .setTitle(
                                randomItem["title"].asText(),
                                randomItem["image"]["contextLink"].asText()
                            )
                            .setImage(randomItem["link"].asText())
                    )
                }

            }
            "lewdneko" -> {
                WebUtils.ins.getJSONObject("https://nekos.life/api/v2/img/lewd").async {
                    sendEmbed(ctx, EmbedUtils.embedImage(it["url"].asText()))
                }
            }
            "lewdkitsune" -> {
                WebUtils.ins.getJSONObject("${nekkobotBase}lewdkitsune").async {
                    sendEmbed(ctx, EmbedUtils.embedImage(it["message"].asText()))
                }
            }
            "hentai" -> {
                val t = if (ctx.random.nextInt(2) == 1) "hentai" else "hentai_anal"
                WebUtils.ins.getJSONObject("$nekkobotBase$t").async {
                    sendEmbed(ctx, EmbedUtils.embedImage(it["message"].asText()))
                }
            }
        }
    }

    private fun parseCommandHelp(invoke: String): String {
        return when (invoke) {
            "lewdneko" -> "Gives a very lewd neko"
            "carsandhentai" -> "Delet this"
            "lewdkitsune" -> "Shows you a lewd kitsune"
            "hentai" -> "Just some hentai"
            else -> "No U"
        }
    }
}
