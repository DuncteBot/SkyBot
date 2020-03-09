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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.weebJava.helpers.QueryBuilder
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils

class UnshortenCommand : Command() {

    init {
        this.name = "unshorten"
        this.helpFunction = { _, _ -> "Gets the long url from a shortened url" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <short url>`" }
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val url = ctx.args[0]

        if (!AirUtils.isURL(url)) {
            sendMsg(ctx, "`$url` is not a valid url")
            return
        }

        val builder = QueryBuilder()
            .append("unshorten")
            .append("url", url)

        val json = ctx.apis.executeDefaultGetRequest(builder.build(), false)

        logger.debug("Unshorten: $json")

        if (!json["success"].asBoolean()) {
            val error = json["error"]
            logger.error("Failed to unshorten $error")
            sendMsg(ctx, "Could not unshorten url: " + error["message"].asText())
            return
        }

        val data = json["data"]

        val embed = EmbedUtils.embedMessage("""Short url:
                            |```
                            |${data["short_url"].asText()}
                            |```
                            |Unshortened url:
                            |```
                            |${data["long_url"].asText()}
                            |```
                        """.trimMargin())

        sendEmbed(ctx, embed)
    }
}
