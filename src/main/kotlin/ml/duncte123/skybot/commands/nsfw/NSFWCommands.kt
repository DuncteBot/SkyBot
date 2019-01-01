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

package ml.duncte123.skybot.commands.nsfw

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

@Author(nickname = "duncte123", author = "Duncan Sterken")
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
                WebUtils.ins.getJSONObject(String.format(ctx.googleBaseUrl, "Cars and hentai")).async { jsonRaw ->
                    val jsonArray = jsonRaw.getJSONArray("items")
                    val randomItem = jsonArray.getJSONObject(ctx.random.nextInt(jsonArray.length()))
                    sendEmbed(event,
                        EmbedUtils.defaultEmbed()
                            .setTitle(randomItem.getString("title"), randomItem.getJSONObject("image")
                                .getString("contextLink"))
                            .setImage(randomItem.getString("link"))
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

    override fun help() = """`${Settings.PREFIX}lewdneko` => Gives a very lewd neko
        |`${Settings.PREFIX}carsandhentai` => Delet this
        |`${Settings.PREFIX}lewdkitsune` => Shows you a lewd kitsune
        |`${Settings.PREFIX}hentai` => Just some hentai
    """.trimMargin()

    override fun help(invoke: String?): String {
        return when (invoke) {
            "lewdneko" -> {
                """Gives a very lewd neko
                    |Usage `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "carsandhentai" -> {
                """Delet this
                    |Usage `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "lewdkitsune" -> {
                """Shows you a lewd kitsune
                    |Usage `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            "hentai" -> {
                """Just some hentai
                    |Usage `${Settings.PREFIX}$invoke`
                """.trimMargin()
            }
            else -> "No U"
        }
    }

    override fun getName() = "lewdneko"

    override fun getAliases() = arrayOf("carsandhentai", "lewdkitsune", "hentai")
}
