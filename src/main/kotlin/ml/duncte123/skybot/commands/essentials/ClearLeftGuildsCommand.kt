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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.extensions.isUnavailable
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev

class ClearLeftGuildsCommand : Command() {
    init {
        this.category = CommandCategory.UNLISTED
        this.name = "clearleftguilds"
        this.help = "Clears the guilds from the db that the bot is no longer in"
    }

    override fun execute(ctx: CommandContext) {
        if (!isDev(ctx.author)) {
            return
        }

        sendMsg(ctx, "Checking settings, please wait")

        val database = ctx.variables.database
        val future = database.getGuildSettings()

        val settings = future.get().filter {
            ctx.shardManager.getGuildById(it.guildId) == null && !ctx.shardManager.isUnavailable(it.guildId)
        }

        if (settings.isEmpty()) {
            sendMsg(ctx, "No access settings to clear")

            return
        }

        sendMsg(ctx, "Deleting ${settings.size} guild settings as we are not in those guilds anymore")

        database.purgeGuildSettings(settings.map { it.guildId })
    }
}
