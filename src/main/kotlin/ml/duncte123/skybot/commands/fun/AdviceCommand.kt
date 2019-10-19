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

import com.github.natanbc.reliqua.request.RequestException
import me.duncte123.botcommons.messaging.EmbedUtils.embedMessageWithTitle
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

class AdviceCommand : Command() {

    init {
        this.category = CommandCategory.FUN
        this.name = "advice"
        this.helpFunction = { _, _ -> "Gives some advice" }
    }

    override fun execute(ctx: CommandContext) {
        try {
            val json = WebUtils.ins.getJSONObject("https://api.adviceslip.com/advice").execute()

            if (json.has("message")) {
                val type = json.get("message").get("type").asText()

                if (type == "error") {
                    sendMsg(ctx, "Something borked: ${json.get("message").get("text").asText()}")

                    return
                }
            }

            val advice = json.get("slip").get("advice").asText()

            sendEmbed(ctx, embedMessageWithTitle("Here's some advice", advice))
        } catch (ex: RequestException) {
            sendMsg(ctx, "An SSL error has occurred and a secure connection to the server cannot be made. - William Shakespeare")
        }
    }
}
