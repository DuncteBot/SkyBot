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

package me.duncte123.skybot.commands.weeb

import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.weebJava.configs.ImageConfig

class MeguminCommand : WeebCommandBase() {
    init {
        this.name = "megumin"
        this.help = "EXPLOSION!!!!!"
    }

    override fun execute(ctx: CommandContext) {
        val quote = ctx.apis.getMeguminQuote()
        val img = ctx.weebApi.getRandomImage(
            ImageConfig.Builder()
                .setType("megumin")
                .build()
        )

        sendEmbed(ctx, getWeebEmbedImageAndDesc(quote, img.execute().url))
    }
}
