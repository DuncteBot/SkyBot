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

package ml.duncte123.skybot.commands.animals

import me.duncte123.botCommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import java.io.IOException

class BirbCommand : Command() {

    init {
        this.category = CommandCategory.ANIMALS
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        try {
            //https://random.birb.pw/
            WebUtils.ins.getJSONObject("https://birdsare.cool/bird.json").async {
                sendEmbed(event, EmbedUtils.embedImage(it.getString("url")))
            }

        } catch (e: IOException) {
            sendMsg(event, "ERROR: " + e.message)
            ComparatingUtils.checkEx(e)
        }
    }

    override fun getName() = "birb"

    override fun help() = "Here is a birb"

    override fun getAliases() = arrayOf("bird")
}