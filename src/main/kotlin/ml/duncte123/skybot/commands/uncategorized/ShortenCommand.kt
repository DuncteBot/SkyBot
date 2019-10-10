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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.isURL
import ml.duncte123.skybot.utils.AirUtils.shortenUrl

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class ShortenCommand : Command() {

    init {
        this.name = "shorten"
        this.aliases = arrayOf("short", "url", "bitly", "googl")
        this.helpFunction = {_,_ -> "Shortens a link"}
        this.usageInstructions = {invoke, prefix -> "`$prefix$invoke <link>`"}
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        if (args.isEmpty() || args[0].isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        if (!isURL(args[0])) {
            sendMsg(event, "That does not look like a valid url")
            return
        }

        shortenUrl(args[0], ctx.config.apis.googl).async({
            sendMsg(event, "Here is your shortened url: <$it>")
        }, {
            sendMsg(event, "Something went wrong, please make sure that your url to shorten is valid")
        })
    }
}
