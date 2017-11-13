/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

class TTBCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(invoke: String?, args: Array<out String>?, event: GuildMessageReceivedEvent?) {
        if (args == null || args.isEmpty()) {
            sendMsg(event, "Correct usage: `${Settings.prefix}${invoke} <words>`")
            return
        }

        val sb = StringBuilder()
        for (a in StringUtils.join(args, " ").toCharArray().map(Character::toString)) {
            if (Character.isLetter(a.toLowerCase()[0])) {
                sb.append(":regional_indicator_").append(a.toLowerCase()).append(":")
            } else {
                if (" " == a) {
                    sb.append(" ")
                }
                sb.append(a)
            }
        }

        sendEmbed(event, EmbedUtils.embedMessage(sb.toString()))
    }

    override fun help() = "ttb test in kotlin"

    override fun getName() = "ttb"
}