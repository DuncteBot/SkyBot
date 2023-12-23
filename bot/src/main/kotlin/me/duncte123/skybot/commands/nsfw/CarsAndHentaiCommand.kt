/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.commands.nsfw

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.skybot.objects.command.Command
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext

class CarsAndHentaiCommand : Command() {
    init {
        this.category = CommandCategory.NSFW
        this.name = "carsandhentai"
        this.help = "Delet this"
    }

    override fun execute(ctx: CommandContext) {
        WebUtils.ins.getJSONObject(String.format(ctx.googleBaseUrl, "Cars and hentai")).async { jsonRaw ->
            val jsonArray = jsonRaw["items"]
            val randomItem = jsonArray[ctx.random.nextInt(jsonArray.size())]
            sendEmbed(
                ctx,
                EmbedUtils.getDefaultEmbed()
                    .setTitle(
                        randomItem["title"].asText(),
                        randomItem["image"]["contextLink"].asText()
                    )
                    .setImage(randomItem["link"].asText())
            )
        }
    }
}
