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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class InstaCommand : Command() {

    init {
        this.name = "insta"
        this.helpFunction = { _, _ -> "Shows the latest picture on someones instagram account" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <username>`" }
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args
        val event = ctx.event

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val username = args.joinToString(separator = "")

        val it = ctx.apis.executeDefaultGetRequest("insta/$username", false)

        if (!it.get("success").asBoolean()) {
            sendMsg(event, "No data found for this user")
            return
        }

        val imagesArray = it.get("images")

        if (imagesArray.size() == 0) {
            sendMsg(ctx, "This user did not upload any images")
            return
        }

        val img = imagesArray.get(0)
        val user = it.get("user")

        val embed = EmbedUtils.defaultEmbed()
            .setAuthor(user.get("username").asText(), "https://instagram.com/$username/", user.get("profile_pic_url").asText())
            .setTitle("Latest picture of $username", img.get("page_url").asText())
            .setDescription(img.get("caption").asText())
            .setImage(img.get("url").asText())

        MessageUtils.sendEmbed(event, embed)

    }
}
