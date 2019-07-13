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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.weebJava.helpers.QueryBuilder
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import ml.duncte123.skybot.utils.AirUtils
import java.io.IOException

class UnsortenCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (ctx.args.isEmpty()) {
            sendMsg(event, "Missing arguments: `${ctx.prefix}$name <short url>`")
            return
        }

        val url = ctx.args[0]

        if (!AirUtils.isURL(url)) {
            sendMsg(event, "`$url` is not a valid url")
            return
        }

        val builder = QueryBuilder()
            .append("https://apis.duncte123.me/unshorten")
            .append("url", url)
            .append("token", event.jda.token)


        WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
            .url(builder.build())
            .addHeader("Accept", WebUtils.EncodingType.APPLICATION_JSON.type)
            .build()) { it.body() }.async(
            { body ->
                try {
                    val res = body.string()
                    logger.debug("Unshorten: $res")
                    val json = ctx.variables.jackson.readTree(res).get("data")

                    val embed = EmbedUtils.embedMessage("""Short url:
                            |```
                            |${json.get("short_url").asText()}
                            |```
                            |Unshortened url:
                            |```
                            |${json.get("long_url").asText()}
                            |```
                        """.trimMargin())

                    sendEmbed(event, embed)
                } catch (e: IOException) {
                    e.printStackTrace()
                    sendMsg(event, "An unknown error occurred.")
                }
            },
            { error ->
                ComparatingUtils.execCheck(error)
                sendMsg(event, "Something went wrong: `${error.message}`")
            }
        )


    }

    override fun getName() = "unshorten"

    override fun help(prefix: String) = """Unshorten a short url
        |Usage: `$prefix$name <short url>`
    """.trimMargin()
}
