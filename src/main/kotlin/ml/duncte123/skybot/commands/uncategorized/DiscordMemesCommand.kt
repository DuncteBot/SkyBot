/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.commands.weeb.WeebCommandBase
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class DiscordMemesCommand : WeebCommandBase() {

    init {
        this.category = CommandCategory.MAIN
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        MessageUtils.sendEmbed(event, getWeebEmbedImage(AirUtils.WEEB_API.getRandomImage("discord_memes").url))
    }

    override fun help() = """Gives you a discord meme
        |Usage: `$PREFIX$name`
    """.trimMargin()

    override fun getName() = "discordmeme"

    override fun getAliases() = arrayOf("dmeme", "discordmemes", "dmemes")
}