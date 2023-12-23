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

class B1nzyCommand : WeebCommandBase() {
    init {
        this.name = "b1nzy"
        this.help = "Shows a b1nzy meme"
    }

    override fun execute(ctx: CommandContext) {
        sendEmbed(ctx, getWeebEmbedImage(ctx.weebApi.getRandomImage(ImageConfig.Builder().setTags(listOf("b1nzy")).build()).execute().url))
    }
}
