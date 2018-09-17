/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import ml.duncte123.skybot.commands.weeb.WeebCommandBase
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed

class DiscordMemesCommand : WeebCommandBase() {

    init {
        this.category = CommandCategory.FUN
        this.displayAliasesInHelp = false
    }

    override fun executeCommand(ctx: CommandContext) {
        ctx.weebApi.getRandomImage("discord_memes").async {
            sendEmbed(ctx.event, getWeebEmbedImage(it.url))
        }
    }

    override fun help() = """Gives you a discord meme
        |Usage: `$PREFIX$name`
    """.trimMargin()

    override fun getName() = "discordmeme"

    override fun getAliases() = arrayOf("dmeme", "discordmemes", "dmemes")
}