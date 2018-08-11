/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

@file:Suppress("MemberVisibilityCanPrivate")

package ml.duncte123.skybot.commands.`fun`

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import java.util.concurrent.TimeUnit

class CoinCommand : Command() {

    private val imagesArr = arrayOf("heads.png", "tails.png")

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        event.channel.sendTyping().queue {
            event.channel.sendMessage("*Flips a coin*").queueAfter(500, TimeUnit.MILLISECONDS) { _ ->
                MessageUtils.sendEmbed(event, EmbedUtils.embedImage("https://duncte123.me/img/coin/"
                        + imagesArr[ctx.random.nextInt(2)]))
            }
        }
    }

    override fun help() = "flips a coin.\nUsage: `$PREFIX$name`"

    override fun getName() = "coin"
}