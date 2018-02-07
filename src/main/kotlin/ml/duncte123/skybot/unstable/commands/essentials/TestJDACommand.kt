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

@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot.unstable.commands.essentials

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.utils.MessageUtils.sendMsg
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

@Author(nickname = "Sanduhr32", author = "Maurice R S")
class TestJDACommand : Command() {
    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        val activity = event.message.activity
        if (activity != null) {
            var base = String.format("Wow you have activity!!!!Type: %d, Party: %s",
                    activity.type.id, activity.partyId)
            activity.application?.let { base += ", and an application!1elf! Name: ${it.name}, ID: ${it.id},DESC ${it.description}" }
            sendMsg(event.channel, base)
            val game = event.member.game
            if (game.isRich) {
                val sesid = game.asRichPresence().sessionId
                sesid?.let { event.channel.sendActivity(activity.setSessionId(it)).queue() }
            }
        } else {
            sendMsg(event.channel, "YOU CRAZY?!")
        }
    }

    override fun help(): String = "MEMES"

    override fun getName(): String = "testjda"

    override fun getAliases(): Array<String> = arrayOf("jda")
}