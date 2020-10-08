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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

@Authors(
    authors = [
        Author(nickname = "Sanduhr32", author = "Maurice R S"),
        Author(nickname = "duncte123", author = "Duncan Sterken"),
        Author(nickname = "ramidzkh", author = "Ramid Khan")
    ]
)
class WebsiteCommand : Command() {

    init {
        this.name = "website"
        this.help = "Shows the bots website"
    }

    override fun execute(ctx: CommandContext) {
        sendMsg(ctx, "My website is <https://dunctebot.com>")
    }
}
