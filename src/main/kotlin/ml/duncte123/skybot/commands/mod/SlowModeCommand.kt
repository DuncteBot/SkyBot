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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.core.Permission

@Author(nickname = "duncte123", author = "Duncan Sterken")
class SlowModeCommand : Command() {

    init {
        this.category = CommandCategory.MOD_ADMIN
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (!event.member.hasPermission(Permission.MESSAGE_MANAGE)) {
            MessageUtils.sendMsg(event, "You don't have the `manage message` permission, this permission is required to run this command!")
            return
        }

        if (!ctx.selfMember.hasPermission(ctx.channel, Permission.MANAGE_CHANNEL)) {
            MessageUtils.sendMsg(event, "I need the `manage channel` permission for this channel in order for this command to work")
            return
        }

        if (ctx.args.isEmpty()) {

            val currentMode = ctx.channel.slowmode
            val currentModeString = if (currentMode == 0) "off" else "${currentMode}ms"

            sendMsg(event, "Current slowmode is `$currentModeString`")
            return
        }

        val delay = ctx.args[0]

        if (delay == "off") {
            ctx.channel.manager.setSlowmode(0).reason("Requested by ${String.format("%#s", ctx.author)}").queue()
            MessageUtils.sendSuccess(ctx.message)
            return
        }

        if (!AirUtils.isInt(delay)) {
            sendMsg(event, "Provided argument is not an integer")
            return
        }

        val intDelay = delay.toInt()

        if (intDelay < 1 || intDelay > 120) {
            sendMsg(event, "$intDelay is not valid, a valid delay is a number in the range 1-120")
            return
        }

        ctx.channel.manager.setSlowmode(intDelay).reason("Requested by ${String.format("%#s", ctx.author)}").queue()
        MessageUtils.sendSuccess(ctx.message)

    }

    override fun getName() = "slowmode"

    override fun help() = """Enable slowmode on in the current channel.
        |Usage: `${Settings.PREFIX}$name <seconds (1-120)>`
        |Use `${Settings.PREFIX}$name off` to turn slowmode off
    """.trimMargin()
}
