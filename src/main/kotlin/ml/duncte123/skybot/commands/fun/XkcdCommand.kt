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

import com.fasterxml.jackson.databind.node.ObjectNode
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import kotlin.math.floor

class XkcdCommand : Command() {
    init {
        this.category = CommandCategory.FUN
        this.name = "xkcd"
        this.helpFunction = { _, _ -> "Sends the latest xkcd comic" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke [latest/random/number]`" }
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            return sendComic(getLatest(), ctx)
        }

        when (args[0]) {
            "latest" -> sendComic(getLatest(), ctx)
            "random" -> sendComic(getInfo(getRandom()), ctx)
            else -> try {
                val comicId = args[0].toInt()

                sendComic(getInfo(comicId), ctx)
            } catch (ignored: NumberFormatException) {
                sendMsg(ctx, "The provided comic id is not a number")
            } catch (other: Exception) {
                sendMsg(ctx, "That comic could not be found")
            }
        }
    }

    private fun sendComic(it: ObjectNode, ctx: CommandContext) {
        val embed = EmbedUtils.defaultEmbed()
            .setTitle(
                it.get("safe_title").asText(),
                "http://xkcd.com/${it.get("num").asInt()}/"
            )
            .setImage(it.get("img").asText())

        sendEmbed(ctx, embed)
    }

    private fun getInfo(id: Int): ObjectNode {
        return WebUtils.ins.getJSONObject("http://xkcd.com/$id/info.0.json").execute()
    }

    private fun getLatest(): ObjectNode {
        return WebUtils.ins.getJSONObject("http://xkcd.com/info.0.json").execute()
    }

    private fun getRandom(): Int {
        val it = getLatest()
        val max = it.get("num").asInt()
        var selected = max.maxRand()

        while (selected == 404) {
            selected = max.maxRand()
        }

        return selected
    }

    private fun Int.maxRand() = floor(Math.random() * this).toInt()
}
