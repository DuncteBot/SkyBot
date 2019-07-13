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

@file:JvmName("DeHoistUtilsKt")

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DeHoistCommand : Command() {
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        if (event.message.mentionedMembers.size == 0) {
            sendMsg(event, """"Incorrect usage
                |Correct usage: `${ctx.prefix}$name <@user>`
            """.trimMargin())
            return
        }
        val toDehoist = event.message.mentionedMembers[0]
        if (!event.guild.selfMember.canInteract(toDehoist)
            || !event.guild.selfMember.hasPermission(Permission.NICKNAME_MANAGE)) {
            sendMsg(event, "I do not have the permission to change that members's nickname")
            return
        }
        if (!event.member.canInteract(toDehoist) || !event.member.hasPermission(Permission.NICKNAME_MANAGE)) {
            sendMsg(event, "You do not have enough permission to execute this command")
            return
        }

        event.guild.controller.setNickname(toDehoist, "\u25AA" + toDehoist.effectiveName)
            .reason("de-hoist by ${event.author.asTag}").queue()
        sendSuccess(event.message)
    }

    override fun help(prefix: String) = """De-hoists a user
        |Usage: `$prefix$name <@user>`
    """.trimMargin()

    override fun getName() = "dehoist"
}

@Author(nickname = "duncte123", author = "Duncan Sterken")
class DeHoistListener(private val variables: Variables) : ListenerAdapter() {

    private val regex = "[!\"#\$%&'()*+,-./](?:.*)".toRegex()
    private val dehoistChar = "▪"

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (shouldChangeName(event.member)) {
            //the char \uD82F\uDCA2 or \u1BCA2 is a null char that puts a member to the bottom
            event.guild.controller.setNickname(event.member, dehoistChar + event.member.effectiveName)
                .reason("auto de-hoist").queue()
        }
    }

    override fun onGuildMemberNickChange(event: GuildMemberNickChangeEvent) {
        if (shouldChangeName(event.member)) {
            //the char \uD82F\uDCA2 or \u1BCA2 is a null char that puts a member to the bottom
            event.guild.controller.setNickname(event.member, dehoistChar + event.member.effectiveName)
                .reason("auto de-hoist").queue()
        }
    }

    /*
     * This checks if we should change the nickname of a member to de-hoist it
     * @return [Boolean] true if we should change the nickname
     */
    private fun shouldChangeName(member: Member): Boolean {
        val memberName = member.effectiveName
        val matcher = regex.matches(memberName)
        return (!memberName.startsWith(dehoistChar) && matcher &&
            member.guild.selfMember.hasPermission(Permission.NICKNAME_MANAGE) &&
            GuildSettingsUtils.getGuild(member.guild, variables).isAutoDeHoist)
    }
}
