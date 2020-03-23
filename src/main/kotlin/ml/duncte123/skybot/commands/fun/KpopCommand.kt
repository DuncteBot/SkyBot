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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.api.DuncteApis
import ml.duncte123.skybot.objects.api.KpopObject
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Author(nickname = "duncte123", author = "Duncan Sterken")
class KpopCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "kpop"
        this.help = "Gives you a random kpop member, command suggestion by Exa"
        this.usage = "[search term]"
    }

    override fun execute(ctx: CommandContext) {
        val queryString = if (ctx.args.isNotEmpty()) ctx.argsRaw else ""
        val member = ctx.variables.apis.getRandomKpopMember(queryString)

        if (member == null) {
            MessageUtils.sendMsg(ctx.event, "Nothing found, but we're open to suggestions")
            return
        }

        val eb = EmbedUtils.defaultEmbed()
            .setDescription("Here is a kpop member from the group ${member.band}")
            .addField("Name of the member", member.name, false)
            .setImage(member.image)
            .setFooter("Query id: ${member.id}", Settings.DEFAULT_ICON)

        sendEmbed(ctx.event, eb.build())
    }

    private fun DuncteApis.getRandomKpopMember(search: String): KpopObject? {
        val path = if (!search.isBlank()) "/${URLEncoder.encode(search, StandardCharsets.UTF_8)}" else ""
        val response = executeDefaultGetRequest("kpop$path", false)

        if (!response["success"].asBoolean()) {
            return null
        }

        val json = response["data"]

        return KpopObject(
            json["id"].asInt(),
            json["name"].asText(),
            json["band"].asText(),
            json["img"].asText()
        )
    }
}
