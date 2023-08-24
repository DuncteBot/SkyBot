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

package ml.duncte123.skybot.commands.music

import lavalink.client.io.filters.Karaoke
import lavalink.client.io.filters.Rotation
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.MusicCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestFilterCommand : MusicCommand() {
    init {
        this.displayAliasesInHelp = false
    }

    override fun run(ctx: CommandContext) {
        val player = getLavalinkManager().lavalink.getLink(ctx.guild).player

        player.filters.rotation = Rotation()
        player.filters.karaoke = Karaoke().apply {
            level = 3f
        }

        player.filters.commit()
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue()
    }

    override fun getName(): String = "testfilter"
}
