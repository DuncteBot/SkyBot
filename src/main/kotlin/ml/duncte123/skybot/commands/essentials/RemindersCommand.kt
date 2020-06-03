/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.essentials

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.api.Reminder
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils

class RemindersCommand : Command() {

    init {
        this.name = "reminders"
        this.aliases = arrayOf("remindmanager")
        this.help = "Shows the reminders that are currently active for you and allows you to manage your reminders"
        this.usage = "[list/cancel/delete/show/info] [reminder id]"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            listReminders(ctx)
            return
        }

        val action = args[0].toLowerCase()

        if (action == "list") {
            listReminders(ctx)
            return
        }

        if (args.size < 2) {
            sendUsageInstructions(ctx)
            return
        }

        val actions = arrayOf("cancel", "delete", "show", "info")

        if (!actions.contains(action)) {
            sendMsg(ctx, "`$action` is an unknown action, available actions are ${actions.joinToString()}")
            return
        }

        val reminderIdStr = args[1]

        if (!AirUtils.isInt(reminderIdStr)) {
            sendMsg(ctx, "`$reminderIdStr` is not a valid id (the id is the number you see in `${ctx.prefix}reminders list`)")
            return
        }

        val reminderId = reminderIdStr.toInt()

        ensureReminderExists(reminderId, ctx) {
            when (action) {
                "info", "show" -> showReminder(it, ctx)

                "cancel", "delete" -> deleteReminder(it, ctx)
            }
        }
    }

    private fun listReminders(ctx: CommandContext) {
        ctx.databaseAdapter.listReminders(ctx.author.idLong) {
            sendMsg(ctx, it.joinToString( separator = "\n"))
        }
    }

    private fun ensureReminderExists(reminderId: Int, ctx: CommandContext, callback: (Reminder) -> Unit) {
        //
    }

    private fun showReminder(reminder: Reminder, ctx: CommandContext) {
        //
    }

    private fun deleteReminder(reminder: Reminder, ctx: CommandContext) {
        //
    }
}
