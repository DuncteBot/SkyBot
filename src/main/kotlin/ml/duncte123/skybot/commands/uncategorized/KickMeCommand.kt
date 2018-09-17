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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botCommons.messaging.MessageUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.ModerationUtils
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit

class KickMeCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event
        val args = ctx.args

        val warningMsg = """**WARNING** this command will kick you from this server
                        |If you are sure that you want to kick yourself off this server use `${PREFIX}kickme YESIMSURE`
                        |By running `${PREFIX}kickme YESIMSURE` you agree that you are responsible for the consequences of this command.
                        |DuncteBot and any of it's developers are not responsible for your own kick by running this command
                    """.trimMargin()
        if (args.isEmpty() || args[0] != "YESIMSURE") {
            MessageUtils.sendMsg(event, warningMsg)
        } else if (!args.isEmpty() && args[0] == "YESIMSURE") {
            //Check for perms
            if (event.guild.selfMember.canInteract(event.member) && event.guild.selfMember.hasPermission(Permission.KICK_MEMBERS)) {
                MessageUtils.sendSuccess(event.message)
                //Kick the user
                MessageUtils.sendMsg(event, "Your kick will commerce in 20 seconds") {
                    it.guild.controller.kick(event.member)
                            .reason("${String.format("%#s", event.author)} ran the kickme command and got kicked")
                            .queueAfter(20L, TimeUnit.SECONDS) {_ ->
                                ModerationUtils.modLog(event.jda.selfUser,
                                        event.author, "kicked", "Used the kickme command", ctx.guild)
                            }
                }
            } else {
                MessageUtils.sendMsg(event, """I'm missing the permission to kick you.
                            |You got lucky this time ${event.member.asMention}.
                        """.trimMargin())
            }
        } else {
            MessageUtils.sendMsg(event, warningMsg)
        }
    }

    override fun help() = """Kickes you off the server
                    |Usage: `$PREFIX$name`
                """.trimMargin()

    override fun getName() = "kickme"
}