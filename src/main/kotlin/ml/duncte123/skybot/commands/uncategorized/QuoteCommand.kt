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
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import java.time.Month
import java.time.OffsetDateTime

class QuoteCommand : Command() {

    init {
        this.name = "quote"
        this.help = "Shows an inspiring quote"
    }

    override fun execute(ctx: CommandContext) {
        WebUtils.ins.getText("http://inspirobot.me/api?${getQ()}").async {
            sendEmbed(ctx, EmbedUtils.embedImage(it))
        }
    }

    private fun getQ() = buildString {
        append("generate=true")

        val date = OffsetDateTime.now()

        if (date.month == Month.DECEMBER && date.dayOfMonth >= 25) {
            append("&season=xmas")
        }
    }
}
