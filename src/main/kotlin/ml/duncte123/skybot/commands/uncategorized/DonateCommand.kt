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

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class DonateCommand : Command() {

    override fun executeCommand(ctx: CommandContext) {
        val amount = if (ctx.args.isNotEmpty()) "/" + ctx.argsRaw else ""

        MessageUtils.sendMsg(ctx.event, """Hey there thank you for your interest in supporting the bot.
                        |You can use one of the following methods to donate:
                        |**Patreon:** <https://patreon.com/DuncteBot>
                        |**PayPal:** <https://paypal.me/duncte123$amount>
                        |
                        |All donations will go directly into development of the bot ❤
                    """.trimMargin())
    }

    override fun getName() = "donate"

    override fun help(prefix: String): String? = """Help keeping the bot up by donating
        |Usage: `$prefix$name [amount]`
    """.trimMargin()
}
