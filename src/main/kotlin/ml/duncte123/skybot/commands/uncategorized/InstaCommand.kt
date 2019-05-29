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
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.api.DuncteApis.Companion.API_HOST
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class InstaCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {
        val args = ctx.args
        val event = ctx.event

        if (args.isEmpty()) {
            MessageUtils.sendMsg(event, "Correct usage: `${ctx.prefix}$name <username>`")
            return
        }

        val username = args.joinToString(separator = "")

        WebUtils.ins.getJSONObject("$API_HOST/insta/$username").async {

            if (!it.get("success").asBoolean()) {
                MessageUtils.sendMsg(event, "No data found for this user")
                return@async
            }

            val img = it.get("images").get(0)
            val user = it.get("user")

            val embed = EmbedUtils.defaultEmbed()
                .setAuthor(user.get("username").asText(), "https://instagram.com/$username/", user.get("profile_pic_url").asText())
                .setTitle("Latest picture of $username", "https://instagram.com/$username/")
                .setDescription(img.get("caption").asText())
                .setImage(img.get("url").asText())

            MessageUtils.sendEmbed(event, embed)
        }

    }

    override fun getName() = "insta"

    override fun help(prefix: String): String? = """Get the latest picture of someones profile
                    |Usage: `$prefix$name <username>`
                """.trimMargin()
}
