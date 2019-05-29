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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.core.Permission

@Author(nickname = "duncte123", author = "Duncan Sterken")
class SlowModeCommand : ModBaseCommand() {

    init {
        this.perms = arrayOf(Permission.MESSAGE_MANAGE)
        this.argscheck = false
    }

    override fun run(ctx: CommandContext) {

        val event = ctx.event

        if (!ctx.selfMember.hasPermission(ctx.channel, Permission.MANAGE_CHANNEL)) {
            sendMsg(event, "I need the `manage channel` permission for this channel in order for this command to work")
            return
        }

        if (ctx.args.isEmpty()) {

            val currentMode = ctx.channel.slowmode
            val currentModeString = if (currentMode == 0) "off" else "$currentMode seconds"

            sendMsg(event, "Current slowmode is `$currentModeString`")
            return
        }

        val delay = ctx.args[0]

        if (delay == "off") {
            ctx.channel.manager.setSlowmode(0).reason("Requested by ${ctx.author.asTag}").queue()
            sendSuccess(ctx.message)
            return
        }

        if (!AirUtils.isInt(delay)) {
            sendMsg(event, "Provided argument is not an integer")
            return
        }

        val intDelay = delay.toInt()

        if (intDelay < 0 || intDelay > 21600) {
            sendMsg(event, "$intDelay is not valid, a valid delay is a number in the range 0-21600 (21600 is 6 hours in seconds)")
            return
        }

        ctx.channel.manager.setSlowmode(intDelay).reason("Requested by ${ctx.author.asTag}").queue()
        sendSuccess(ctx.message)

    }

    override fun getName() = "slowmode"

    override fun help(prefix: String): String? = """Enable slowmode on in the current channel.
        |Usage: `$prefix$name <seconds (1-120)>`
        |Use `$prefix$name off` to turn slowmode off
    """.trimMargin()
}
