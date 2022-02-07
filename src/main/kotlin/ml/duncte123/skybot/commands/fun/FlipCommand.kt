/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.`fun`

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import kotlin.math.floor

class FlipCommand : Command() {

    private val flips = arrayOf(
        "(╯°□°）╯︵",
        "(┛◉Д◉)┛彡┻━┻",
        "(ﾉ≧∇≦)ﾉ ﾐ ┻━┻",
        "(ノಠ益ಠ)ノ彡┻━┻",
        "(╯ರ ~ ರ)╯︵ ┻━┻",
        "(┛ಸ_ಸ)┛彡┻━┻"
    )

    init {
        this.category = CommandCategory.FUN
        this.name = "flip"
        this.help = "Flips someone upside down"
        this.usage = "[@user/text]"
    }

    override fun execute(ctx: CommandContext) {
        var uname = ctx.member!!.effectiveName

        if (ctx.message.mentionedMembers.isNotEmpty()) {
            uname = ctx.message.mentionedMembers[0].effectiveName
        } else if (ctx.args.isNotEmpty()) {
            uname = ctx.argsJoined
        }

        var normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'"
        var split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,"
        // maj
        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ"
        // number
        normal += "0123456789"
        split += "0ƖᄅƐㄣϛ9ㄥ86"

        uname = uname.reversed()

        val output = buildString {
            for (letter in uname.iterator()) {
                val a = normal.indexOf(letter.toString(), 0)

                append(if (a != -1) split[a] else letter)
            }
        }

        sendMsg(ctx, "${getFlip()} $output")
    }

    private fun getFlip() = flips[floor(Math.random() * flips.size).toInt()]
}
