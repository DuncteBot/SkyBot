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

package me.duncte123.skybot.commands.funCmds

import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.skybot.commands.weeb.WeebCommandBase
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.weebJava.configs.ImageConfig

class DeletCommand : WeebCommandBase() {
    init {
        this.displayAliasesInHelp = false
        this.category = CommandCategory.FUN
        this.name = "delet"
        this.aliases = arrayOf("deletthis", "deletethis")
        this.help = "Delet this"
    }

    override fun execute(ctx: CommandContext) {
        // delet_this
        ctx.weebApi.getRandomImage(
            ImageConfig.Builder()
                .setType("delet_this")
                .build()
        ).async { sendEmbed(ctx, getWeebEmbedImage(it.url)) }
    }
}
