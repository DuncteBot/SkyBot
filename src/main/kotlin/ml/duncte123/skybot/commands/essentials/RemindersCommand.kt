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

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
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
            showRemindersList(ctx)
            return
        }

        val action = args[0].toLowerCase()

        if (action == "list") {
            showRemindersList(ctx)
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

    private fun showRemindersList(ctx: CommandContext) {
        ctx.databaseAdapter.listReminders(ctx.author.idLong) {
            if (it.isEmpty()) {
                sendMsg(ctx, "You don't have any reminders set currently")
                return@listReminders
            }

            sendEmbed(ctx, EmbedUtils.embedMessage(it.joinToString(separator = "\n")))
        }
    }

    private fun ensureReminderExists(reminderId: Int, ctx: CommandContext, callback: (Reminder) -> Unit) {
        ctx.databaseAdapter.showReminder(reminderId, ctx.author.idLong) {
            if (it == null) {
                sendMsg(ctx, "Reminder with id `$reminderId` was not found")
                return@showReminder
            }

            callback(it)
        }
    }

    private fun showReminder(reminder: Reminder, ctx: CommandContext) {
        val remindChannel = if (reminder.channel_id > 0) "<#${reminder.channel_id}>" else "Direct Messages"
        val reminderInfo = """**Id:** ${reminder.id}
            |**Message:** ${reminder.reminder}
            |**Remind in:** $remindChannel
            |**Created:** ${reminder.create_date}
            |**Remind on:** ${reminder.reminder_date}
        """.trimMargin()

        sendEmbed(ctx, EmbedUtils.embedMessage(reminderInfo))
    }

    private fun deleteReminder(reminder: Reminder, ctx: CommandContext) {
        val args = ctx.args

        if (args.size >= 3 && args[2].toLowerCase() == "--just-flipping-do-it") {
            ctx.databaseAdapter.removeReminder(reminder) {
                sendMsg(ctx, "Successfully deleted reminder with id `${reminder.id}`")
            }
            return
        }

        sendMsg(ctx, "To prevent accidental deleting of reminders, you will need to confirm that you want to delete this reminder.\n" +
            "To confirm that you want to delete the reminder please run the following command" +
            "`${ctx.prefix}reminders delete ${reminder.id} --just-flipping-do-it`"
        )
    }
}
