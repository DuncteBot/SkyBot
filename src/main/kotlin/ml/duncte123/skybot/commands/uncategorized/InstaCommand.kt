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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class InstaCommand : Command() {

    init {
        this.name = "insta"
        this.aliases = arrayOf("instagram")
        this.help = "Shows the latest picture on someones instagram account"
        this.usage = "<username>"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val username = args.joinToString(separator = "")
        val it = ctx.apis.executeDefaultGetRequest("insta/$username")

        if (!it["success"].asBoolean()) {
            sendMsg(ctx, "Failed to fetch data: ${it["error"]["message"].asText()}")
            return
        }

        val imagesArray = it["data"]["images"]

        if (imagesArray.isEmpty) {
            sendMsg(ctx, "This user did not upload any images")
            return
        }

        val img = imagesArray[0]
        val user = it["data"]["user"]

        val embed = EmbedUtils.getDefaultEmbed()
            .setAuthor(user["username"].asText(), "https://instagram.com/$username/", user["profile_pic_url"].asText())
            .setTitle("Latest picture of $username", img["page_url"].asText())
            .setDescription(img["caption"].asText())
            .setImage(img["url"].asText())

        sendEmbed(ctx, embed)
    }
}
