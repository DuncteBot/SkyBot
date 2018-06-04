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

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class KpopCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        val queryString = if (!args.isEmpty()) args.joinToString(separator = "%20", prefix = "?search=") else ""
        val url = "${Settings.API_BASE}/kpop/json$queryString"
        WebUtils.ins.getJSONObject(url).async {
            if (!it.optString("name").isBlank()) {
                val eb = EmbedUtils.defaultEmbed()
                        .setDescription("Here is a kpop member from the group ${it.getString("band")}")
                        .addField("Name of the member", it.getString("name"), false)
                        .setImage(it.getString("image"))
                        .setFooter("Query id: ${it.getString("id")}", Settings.DEFAULT_ICON)
                MessageUtils.sendEmbed(event, eb.build())
            } else {
                // nothing found
                MessageUtils.sendMsg(event, "Nothing found")
            }
        }
    }

    override fun help() = "Gives you a random kpop member, command suggestion by Exa\n" +
            "Usage: `$PREFIX$name [search term]`"

    override fun getName() = "kpop"
}