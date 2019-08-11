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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.MessageEmbed
import java.util.function.BiFunction

@Author(nickname = "duncte123", author = "Duncan Sterken")
class TextToBricksCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "ttb"
        this.helpFunction = BiFunction { _, _ -> "Converts your text into emoji bricks" }
        this.usageInstructions = BiFunction { invoke, prefix -> "`$prefix$invoke <text>`" }
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val message = ctx.argsRaw
            .toLowerCase()
            .replace("([a-zA-Z])".toRegex(), ":regional_indicator_\$1:")
            .replace("([0-9])".toRegex(), "\$1\u20E3")
            .replace("!!", "\u203C")
            .replace("!", "\u2757")
            .replace("?", "\u2753")

        if (message.length > MessageEmbed.TEXT_MAX_LENGTH) {
            sendMsg(ctx.event, "Your input is too long, please limit it (${message.length} out of ${MessageEmbed.TEXT_MAX_LENGTH} max chars)")

            return
        }

        sendEmbed(ctx.event, EmbedUtils.embedMessage(message))
    }
}
