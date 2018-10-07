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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.ApiUtils
import java.sql.SQLException

@Author(nickname = "duncte123", author = "Duncan Sterken")
class KpopCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(ctx: CommandContext) {

        try {
            val queryString = if (!ctx.args.isEmpty()) ctx.argsRaw else ""
            val member = ApiUtils.getRandomKpopMember(ctx.database, queryString)
            val eb = EmbedUtils.defaultEmbed()
                .setDescription("Here is a kpop member from the group ${member.band}")
                .addField("Name of the member", member.name, false)
                .setImage(member.image)
                .setFooter("Query id: ${member.id}", Settings.DEFAULT_ICON)
            sendEmbed(ctx.event, eb.build())
        } catch (ignored: SQLException) {
            MessageUtils.sendMsg(ctx.event, "Nothing found")
        }
    }

    override fun help() = "Gives you a random kpop member, command suggestion by Exa\n" +
        "Usage: `$PREFIX$name [search term]`"

    override fun getName() = "kpop"
}
