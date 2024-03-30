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

package me.duncte123.skybot.objects

import me.duncte123.skybot.Variables
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import kotlin.math.min
import me.duncte123.skybot.objects.command.Command as SkyCommand

abstract class SlashSupport : SkyCommand() {
    protected abstract fun configureSlashSupport(baseData: SlashCommandData)

    fun getSlashData(): SlashCommandData {
        val help = getHelp("", "/")

        val base = Commands.slash(
            name,
            help.substring(
                0 until min(help.length, CommandData.MAX_DESCRIPTION_LENGTH)
            )
        )
            .setGuildOnly(true)
            .setNSFW(category == CommandCategory.NSFW)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(this.userPermissions.toList()))

        configureSlashSupport(base)

        return base
    }

    fun executeEventWithChecks(event: SlashCommandInteractionEvent, variables: Variables) {
        if (event.isFromGuild) {
            val self = event.guild!!.selfMember

            if (this.botPermissions.isNotEmpty() && !self.hasPermission(this.botPermissions.toList())) {
                val permissionsWord = "permission${if (this.botPermissions.size > 1) "s" else ""}"
                val permsList = AirUtils.parsePerms(this.botPermissions)

                event.reply(
                    "I need the `$permsList` $permissionsWord for this command to work\n" +
                        "Please contact your server administrator about this."
                )
                    .setEphemeral(false)
                    .queue()
            }

            // TODO: cooldowns

            handleEvent(event, variables)
        } else {
            handleEvent(event, variables)
        }
    }

    abstract fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables)
}
