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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import ml.duncte123.skybot.extensions.abbreviate
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.MessageEmbed

class MemeCommand : Command() {
    init {
        this.category = CommandCategory.FUN
        this.name = "meme"
        this.help = "See a funny meme"
    }

    override fun execute(ctx: CommandContext) {
        val json = ctx.apis.executeDefaultGetRequest("meme", false)["data"]

        val embed = EmbedUtils.defaultEmbed()
            .setTitle(json["title"].asText().abbreviate(MessageEmbed.TITLE_MAX_LENGTH), json["url"].asText())
            .setDescription(json["body"].asText().abbreviate(MessageEmbed.TEXT_MAX_LENGTH))

        if (json.has("image")) {
            embed.setImage(json["image"].asText())
        }

        MessageUtils.sendEmbed(ctx, embed)
    }
}
