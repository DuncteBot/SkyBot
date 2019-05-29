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

package ml.duncte123.skybot.commands.animals

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import java.io.IOException

@Author(nickname = "duncte123", author = "Duncan Sterken")
class BirbCommand : Command() {

    init {
        this.category = CommandCategory.ANIMALS
    }

    override fun executeCommand(ctx: CommandContext) {
        try {
            WebUtils.ins.getJSONArray("https://shibe.online/api/birds").async {
                sendEmbed(ctx.event, EmbedUtils.embedImage(it.get(0).asText()))
            }

        } catch (e: IOException) {
            sendMsg(ctx.event, "ERROR: " + e.message)
        }
    }

    override fun getName() = "birb"

    override fun help(prefix: String): String? = "Here is a birb"

    override fun getAliases() = arrayOf("bird")
}
