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

import com.afollestad.ason.Ason
import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import org.apache.commons.lang3.StringUtils

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
            WebUtils.ins.getText(String.format(ctx.googleBaseUrl, keyword)).async {
                val jsonRaw = Ason(it)
                val jsonArray = jsonRaw.getJsonArray<Ason>("items")
                val randomItem = jsonArray.getJsonObject(ctx.random.nextInt(jsonArray.size()))
                sendEmbed(event,
                        EmbedUtils.defaultEmbed()
                                .setTitle(randomItem!!.getString("title"), randomItem.getString("image.contextLink"))
                                .setImage(randomItem.getString("link")).build()
                )
            }

        }
    }

    override fun help() = """Searches for an image on google
        |Usage: `$PREFIX$name <search term>`""".trimMargin()

    override fun getName() = "image"
}