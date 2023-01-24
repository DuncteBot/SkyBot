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

package ml.duncte123.skybot.commands.essentials

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.api.Reminder
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.utils.TimeFormat

class RemindersCommand : Command() {

    init {
        this.name = "reminders"
        this.aliases = arrayOf("remindmanager", "reminder")
        this.help = "Shows the reminders that are currently active for you and allows you to manage your reminders"
        this.usage = "[list/cancel/delete/show/info] [reminder id]"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            showRemindersList(ctx)
            return
        }

        val action = args[0].lowercase()

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
        ctx.database.listReminders(ctx.author.idLong).thenAccept {
            if (it.isEmpty()) {
                sendMsg(ctx, "You do not have any currently active reminders")
                return@thenAccept
            }

            sendEmbed(ctx, EmbedUtils.embedMessage(it.joinToString(separator = "\n")))
        }
    }

    private fun ensureReminderExists(reminderId: Int, ctx: CommandContext, callback: (Reminder) -> Unit) {
        ctx.database.showReminder(reminderId, ctx.author.idLong).thenAccept {
            if (it == null) {
                sendMsg(ctx, "Reminder with id `$reminderId` was not found")
                return@thenAccept
            }

            callback(it)
        }
    }

    private fun showReminder(reminder: Reminder, ctx: CommandContext) {
        val remindChannel = if (reminder.in_channel) "<#${reminder.channel_id}>" else "Direct Messages"
        val reminderInfo = """**Id:** ${reminder.id}
            |**Message:** ${reminder.reminder}
            |**Remind in:** $remindChannel
            |**Created:** ${reminder.reminderCreateDateDate}
            |**Remind on:** ${reminder.reminderDateDate} (${TimeFormat.RELATIVE.format(reminder.reminder_date)})
            |** Message link:** ${reminder.jumpUrl}
        """.trimMargin()

        sendEmbed(ctx, EmbedUtils.embedMessage(reminderInfo))
    }

    private fun deleteReminder(reminder: Reminder, ctx: CommandContext) {
        val args = ctx.args

        if (args.size >= 3 && args[2].lowercase() == "--just-honking-do-it") {
            ctx.database.removeReminder(reminder).thenAccept {
                sendMsg(ctx, "Successfully deleted reminder with id `${reminder.id}`")
            }
            return
        }

        sendMsg(
            ctx,
            "To prevent accidental deleting of reminders, you will need to confirm that you want to delete this reminder.\n" +
                "To confirm that you want to delete the reminder please run the following command" +
                "`${ctx.prefix}reminders delete ${reminder.id} --just-honking-do-it`"
        )
    }
}
