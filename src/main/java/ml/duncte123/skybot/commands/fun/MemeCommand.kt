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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

class MemeCommand : Command() {
    init {
        this.category = CommandCategory.FUN
        this.name = "meme"
        this.helpFunction = { _, _ -> "See a funny meme" }
    }

    override fun execute(ctx: CommandContext) {
        val json = ctx.apis.executeDefaultGetRequest("meme", false).get("data")

        val embed = EmbedUtils.defaultEmbed()
            .setTitle(json.get("title").asText(), json.get("url").asText())
            .setDescription(json.get("body").asText())

        if (json.has("image")) {
            embed.setImage(json.get("image").asText())
        }

        MessageUtils.sendEmbed(ctx, embed)
    }
}
