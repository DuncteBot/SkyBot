/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.natanbc.reliqua.request.PendingRequest
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext

class XkcdCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {
        getRandom { num ->
            getInfo(num).async {
                val embed = EmbedUtils.defaultEmbed()
                    .setTitle(
                        it.get("safe_title").asText(),
                        "http://xkcd.com/$num/"
                    )
                    .setImage(it.get("img").asText())

                sendEmbed(ctx, embed)
            }
        }
    }

    override fun getName() = "xkcd"

    override fun help() = "Shows a random xkcd comic"

    private fun getInfo(id: Int): PendingRequest<ObjectNode> {
        return WebUtils.ins.getJSONObject("http://xkcd.com/$id/info.0.json")
    }

    private fun getRandom(callback: (Int) -> Unit) {
        WebUtils.ins.getJSONObject("http://xkcd.com/info.0.json").async {
            val max = it.get("num").asInt()
            var selected = max.maxRand()

            while (selected == 404) {
                selected = max.maxRand()
            }

            callback.invoke(selected)
        }
    }

    private fun Int.maxRand() = Math.floor(Math.random() * this).toInt()
}
