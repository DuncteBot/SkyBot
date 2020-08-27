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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.entities.Guild
import java.math.BigInteger
import java.security.MessageDigest

class RolesCommand : Command() {

    init {
        this.name = "roles"
        this.help = "Returns a list of roles on the server"
    }

    override fun execute(ctx: CommandContext) {
        val domain = if (Settings.IS_LOCAL) "http://localhost:2000" else "https://dashboard.dunctebot.com"
        val guild = ctx.jdaGuild
        val hash = generateHash(guild)

        // TODO: find a way to implement this in the remote dashboard (websocket?)
        //GuildStuffController.guildHashes.put(hash, guild.idLong)
        sendMsg(ctx, "Check out the roles on this server here: $domain/roles/$hash\nThis link is valid for 2 hours")
    }

    private fun generateHash(guild: Guild): String {
        val md = MessageDigest.getInstance("MD5")
        val content = guild.id + System.currentTimeMillis()
        val digest = md.digest(content.toByteArray())

        return BigInteger(1, digest).toString(16).padStart(32, '0')
    }
}
