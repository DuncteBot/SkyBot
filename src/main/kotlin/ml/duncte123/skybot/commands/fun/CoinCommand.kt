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

import me.duncte123.botcommons.messaging.EmbedUtils.embedImage
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

@Author(nickname = "duncte123", author = "Duncan Sterken")
class CoinCommand : Command() {

    private val imagesArr = arrayOf("heads.png", "tails.png")

    init {
        this.category = CommandCategory.FUN
        this.name = "coin"
        this.aliases = arrayOf("coinflip", "cf")
        this.helpFunction = BiFunction { _, _ -> "Flips a coin" }
    }

    override fun execute(ctx: CommandContext) {
        ctx.channel.sendMessage("*Flips a coin*").queueAfter(500, TimeUnit.MILLISECONDS) {
            sendEmbed(ctx, embedImage("https://duncte123.me/img/coin/${imagesArr[ctx.random.nextInt(2)]}"))
        }
    }
}
