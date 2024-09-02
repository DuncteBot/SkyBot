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

package me.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class WarningsCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.name = "warnings"
        this.help = "Shows the active warnings that a member has"
        this.usage = "<@user>"
        this.userPermissions = arrayOf(Permission.KICK_MEMBERS)
    }

    override fun execute(ctx: CommandContext) {
        sendMsg(ctx, "This is a slash command now, sorry not sorry")
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addOptions(
            OptionData(
                OptionType.USER,
                "user",
                "The user to check warnings for",
                true
            )
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        event.deferReply().queue()

        val user = event.getOption("user")!!.asUser

        variables.database.getWarningsForUser(user.idLong, guild.idLong).thenAccept { warnings ->
            if (warnings.isEmpty()) {
                event.hook.editOriginal("This member has no active warnings").queue()

                return@thenAccept
            }

            val out = buildString {
                warnings.forEach {
                    val mod = event.jda.getUserById(it.modId)
                    val modName = mod?.asTag ?: "Unknown#0000"
                    val reason = if (it.reason.isNotBlank()) it.reason else "None"

                    appendLine("`[${it.rawDate}]` Reason: _${reason}_ by $modName")
                }
            }

            event.hook.editOriginalEmbeds(
                EmbedUtils.embedMessage(out).setColor(guild.color).build()
            ).queue()
        }
    }
}
