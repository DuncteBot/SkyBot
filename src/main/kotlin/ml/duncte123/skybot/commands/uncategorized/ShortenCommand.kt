/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.isURL
import ml.duncte123.skybot.utils.AirUtils.shortenUrl

class ShortenCommand : Command() {

    init {
        this.requiresArgs = true
        this.name = "shorten"
        this.aliases = arrayOf("short", "url", "bitly", "googl")
        this.help = "Shortens a link"
        this.usage = "<link>"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (!isURL(args[0])) {
            sendMsg(ctx, "That does not look like a valid url")
            return
        }

        shortenUrl(args[0], ctx.config.apis.googl, ctx.variables.jackson).async(
            {
                sendMsg(ctx, "Here is your shortened url: <$it>")
            },
            {
                sendMsg(ctx, "Something went wrong, please make sure that your url to shorten is valid")
            }
        )
    }
}
