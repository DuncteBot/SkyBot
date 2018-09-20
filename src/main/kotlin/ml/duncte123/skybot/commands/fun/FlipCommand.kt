/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext

@Author(nickname = "duncte123", author = "Duncan Sterken")
class FlipCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        var uname = event.member.effectiveName
        var output = ""

        if (event.message.mentionedUsers.size > 0) {
            uname = event.guild.getMember(event.message.mentionedUsers[0])!!.effectiveName
        } else if (ctx.args.isNotEmpty()) {
            uname = ctx.argsJoined
        }

        var normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'"
        var split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,"
        //maj
        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ"
        //number
        normal += "0123456789"
        split += "0ƖᄅƐㄣϛ9ㄥ86"


        uname = uname.reversed()

        for (letter in uname.iterator()) {

            val a = normal.indexOf(letter.toString(), 0)
            output += if (a != -1) split[a] else letter
        }

        MessageUtils.sendMsg(event, "(╯°□°）╯︵ $output")
    }

    override fun help() = "Flips a user.\n" +
            "Usage: `$PREFIX$name [@user]`"

    override fun getName() = "flip"
}