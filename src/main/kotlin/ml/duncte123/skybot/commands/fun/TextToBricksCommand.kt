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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.MessageEmbed

class TextToBricksCommand : Command() {

    init {
        this.requiresArgs = true
        this.category = CommandCategory.FUN
        this.name = "ttb"
        this.help = "Converts your text into emoji bricks"
        this.usage = "<text>"
    }

    override fun execute(ctx: CommandContext) {
        val message = ctx.argsRaw
            .toLowerCase()
            .replace("([a-zA-Z])".toRegex(), ":regional_indicator_\$1:")
            .replace("([0-9])".toRegex(), "\$1\u20E3")
            .replace("!!", "\u203C")
            .replace("!", "\u2757")
            .replace("?", "\u2753")

        if (message.length > MessageEmbed.TEXT_MAX_LENGTH) {
            sendMsg(ctx, "Your input is too long, please limit it (${message.length} out of ${MessageEmbed.TEXT_MAX_LENGTH} max chars)")

            return
        }

        sendEmbed(ctx, EmbedUtils.embedMessage(message))
    }
}
