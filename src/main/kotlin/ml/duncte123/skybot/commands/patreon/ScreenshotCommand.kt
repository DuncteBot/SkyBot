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

package ml.duncte123.skybot.commands.patreon

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.objects.CooldownScope
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.Permission

class ScreenshotCommand : Command() {

    init {
        this.requiresArgs = true
        this.category = CommandCategory.PATRON
        this.name = "screenshot"
        this.help = "Screenshots a website"
        this.usage = "<webpage url>"
        this.botPermissions = arrayOf(Permission.MESSAGE_ATTACH_FILES)
        this.cooldown = 10
        this.cooldownScope = CooldownScope.GUILD
    }

    override fun execute(ctx: CommandContext) {
        val url = ctx.argsRaw

        if (!AirUtils.isURL(url)) {
            sendMsg(ctx, "`$url` is not a valid link")
            return
        }

        // decode base64 to byte array
        val base64 = ctx.apis.screenshotWebsite(url)

        ctx.channel.sendFile(base64, "screenshot.png")
            .append("><").append(url).append('>')
            .queue()
    }
}
