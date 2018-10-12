/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import org.apache.commons.lang3.StringUtils

@Author(nickname = "duncte123", author = "Duncan Sterken")
class ImageCommand : Command() {

    init {
        this.category = CommandCategory.PATRON
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (isUserOrGuildPatron(event)) {
            if (ctx.args.isEmpty()) {
                MessageUtils.sendMsg(event, "Incorrect usage: `$PREFIX$name <search term>`")
                return
            }
            val keyword = StringUtils.join(ctx.args, "+")
            WebUtils.ins.getJSONObject(String.format(ctx.googleBaseUrl, keyword)).async {
                val jsonArray = it.getJSONArray("items")
                val randomItem = jsonArray.getJSONObject(ctx.random.nextInt(jsonArray.length()))
                sendEmbed(event,
                    EmbedUtils.defaultEmbed()
                        .setTitle(randomItem.getString("title"), randomItem.getString("image.contextLink"))
                        .setImage(randomItem.getString("link")).build()
                )
            }

        }
    }

    override fun help() = """Searches for an image on google
        |Usage: `$PREFIX$name <search term>`""".trimMargin()

    override fun getName() = "image"
}
