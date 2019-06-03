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

import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.commands.weeb.WeebCommandBase
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DeletCommand : WeebCommandBase() {
    init {
        this.category = CommandCategory.FUN
        this.displayAliasesInHelp = false
    }

    override fun executeCommand(ctx: CommandContext) {
        //delet_this
        ctx.weebApi.getRandomImage("delet_this").async {
            sendEmbed(ctx.event, getWeebEmbedImage(it.url))
        }
    }

    override fun help(prefix: String): String? = """Delet this
        |Usage: `$prefix$name`
    """.trimMargin()

    override fun getName() = "delet"

    override fun getAliases() = arrayOf("deletthis", "deletethis")
}
