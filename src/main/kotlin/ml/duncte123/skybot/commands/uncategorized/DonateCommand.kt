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

import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.Settings.PATREON
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class DonateCommand : Command() {

    init {
        this.name = "donate"
        this.help = "Help to keep the bot online by donating"
    }

    override fun execute(ctx: CommandContext) {
        val amount = if (ctx.args.isNotEmpty()) "/" + ctx.argsRaw else ""

        MessageUtils.sendMsg(
            ctx,
            """Hey there thank you for your interest in supporting the bot.
                        |You can use one of the following methods to donate:
                        |**Patreon:** <$PATREON>
                        |**PayPal:** <https://paypal.me/duncte123$amount>
                        |
                        |All donations will go directly into development of the bot ❤
                    """.trimMargin()
        )
    }
}
