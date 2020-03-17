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

package ml.duncte123.skybot.commands.weeb

import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.weebJava.types.HiddenMode
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.MessageBuilder

@Author(nickname = "duncte123", author = "Duncan Sterken")
class WeebCommand : WeebCommandBase() {
    private val weebTags = ArrayList<String>()

    init {
        this.name = "weeb"
        this.help = "Gives you a random image from weeb.sh with that type"
        this.usage = "<category>"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            sendMsg(ctx, "Please supply a valid category, Use `${ctx.prefix}weeb categories` for all categories")
            return
        }

        if (weebTags.isEmpty()) {
            weebTags.addAll(ctx.weebApi.getTypes(HiddenMode.DEFAULT).execute().types)
        }

        if (args[0] == "categories") {
            sendMsg(ctx, MessageBuilder()
                .append("Here is a list of all the valid categories")
                .appendCodeBlock(weebTags.joinToString(), "LDIF")
                .build())
            return
        }

        val type = args.joinToString("")

        if (!weebTags.contains(type)) {
            sendMsg(ctx, "That category could not be found, Use `${ctx.prefix}weeb categories` for all categories")
            return
        }

        val img = ctx.weebApi.getRandomImage(type).execute()
        sendEmbed(ctx, getWeebEmbedImageAndDesc("Image ID: ${img.id}", img.url))
    }
}
