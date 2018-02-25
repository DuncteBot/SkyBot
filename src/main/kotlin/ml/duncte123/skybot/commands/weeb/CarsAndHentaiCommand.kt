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

package ml.duncte123.skybot.commands.weeb

import com.afollestad.ason.Ason
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class CarsAndHentaiCommand : WeebCommandBase() {

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        if(!event.channel.isNSFW) {
            MessageUtils.sendMsg(event, """WHoops, this channel is not marked as NSFW.
                |Please mark this channel as NSFW to use this command
                """.trimMargin())
            return
        }

        val jsonRaw = Ason(WebUtils.getText(String.format(AirUtils.GOOGLE_BASE_URL, "Cars and hentai")))
        val jsonArray = jsonRaw.getJsonArray<Ason>("items")
        val randomItem = jsonArray.getJsonObject(AirUtils.RAND.nextInt(jsonArray.size()))
        MessageUtils.sendEmbed(event,
                EmbedUtils.defaultEmbed()
                        .setTitle(randomItem!!.getString("title"), randomItem.getString("image.contextLink"))
                        .setImage(randomItem.getString("link")).build()
        )



    }

    override fun help() = "NO"

    override fun getName() = "carsandhentai"
}