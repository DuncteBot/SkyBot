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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed

class TextToBricksCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(ctx: CommandContext) {
        if (ctx.args.isEmpty()) {
            MessageUtils.sendMsg(ctx.event, "Correct usage: `$PREFIX${ctx.invoke} <words>`")
            return
        }

        sendEmbed(ctx.event,
                EmbedUtils.embedMessage(
                        ctx.rawArgs
                                .toLowerCase()
                                .replace(Regex("([a-zA-Z])"), ":regional_indicator_\$1:")
                                .replace(Regex("([0-9])"), "\$1\u20E3")
                                .replace("!!", ":bangbang:")
                                .replace("!", ":exclamation:")
                                .replace("?", ":question:")
                )
        )
    }

    override fun help() = "Convert your text to bricks"

    override fun getName() = "ttb"
}
