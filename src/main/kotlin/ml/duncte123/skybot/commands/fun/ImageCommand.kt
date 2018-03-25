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

import com.afollestad.ason.Ason
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.*
import ml.duncte123.skybot.utils.MessageUtils.sendEmbed
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class ImageCommand : Command() {

    init {
        this.category = CommandCategory.PATRON
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        //This command is hidden and needs patreon :P
        /* if(!hasUpvoted(event.author)) {
             sendEmbed(event,
                     EmbedUtils.embedMessage("This command is a hidden command, hidden commands are not available to users that have not upvoted the bot, " +
                             "Please consider to give this bot an upvote over at " +
                             "[https://discordbots.org/bot/210363111729790977](https://discordbots.org/bot/210363111729790977)\n" +
                             "\uD83D\uDDD2: The check might be limited and would have a minimum cooldown of 20 seconds!"))
             return
         }*/
        if (isPatron(event.author, event.channel)) {
            if (args.isEmpty()) {
                MessageUtils.sendMsg(event, "Incorrect usage: `$PREFIX$name <search term>`")
                return
            }
            val keyword = StringUtils.join(args, "+")
            WebUtils.ins.getText(String.format(AirUtils.GOOGLE_BASE_URL, keyword)).async {
                val jsonRaw = Ason(it)
                val jsonArray = jsonRaw.getJsonArray<Ason>("items")
                val randomItem = jsonArray.getJsonObject(AirUtils.RAND.nextInt(jsonArray.size()))
                sendEmbed(event,
                        EmbedUtils.defaultEmbed()
                                .setTitle(randomItem!!.getString("title"), randomItem.getString("image.contextLink"))
                                .setImage(randomItem.getString("link")).build()
                )
            }

        }
    }

    override fun help() = """Searches for an image on google
        |Usage: `$PREFIX$name <search term>`""".trimMargin()

    override fun getName() = "image"
}