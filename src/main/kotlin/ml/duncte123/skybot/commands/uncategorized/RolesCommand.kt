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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.MessageBuilder.SplitPolicy

class RolesCommand : Command() {

    init {
        this.name = "roles"
        this.helpFunction = { _, _ -> "Returns a list of roles on the server" }
    }

    override fun execute(ctx: CommandContext) {
        val rolesString = ctx.guild.roleCache.joinToString(separator = "\n") { "@${it.name} - ${it.id}" }
        val messages = MessageBuilder().appendCodeBlock(rolesString, "").buildAll(SplitPolicy.NEWLINE)

        messages.forEach {
            sendMsg(ctx, it)
        }
    }
}
