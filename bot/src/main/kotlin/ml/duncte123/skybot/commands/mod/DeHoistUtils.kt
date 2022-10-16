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

package ml.duncte123.skybot.commands.mod

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DeHoistCommand : ModBaseCommand() {

    init {
        this.requiresArgs = true
        this.name = "dehoist"
        this.help = "De-hoists a user"
        this.usage = "<@user>"
        this.userPermissions = arrayOf(Permission.NICKNAME_MANAGE)
        this.botPermissions = arrayOf(Permission.NICKNAME_MANAGE)
    }

    override fun execute(ctx: CommandContext) {
        if (ctx.message.mentionedMembers.size == 0) {
            this.sendUsageInstructions(ctx)
            return
        }

        val toDehoist = ctx.message.mentionedMembers[0]
        val selfMember = ctx.guild.selfMember
        val member = ctx.member

        if (!selfMember.canInteract(toDehoist)) {
            sendMsg(ctx, "I cannot alter that user")
            return
        }
        if (!member.canInteract(toDehoist)) {
            sendMsg(ctx, "You cannot change the nickname of that user")
            return
        }

        ctx.guild.modifyNickname(toDehoist, "\u25AA" + toDehoist.effectiveName)
            .reason("de-hoist ctx ${ctx.author.asTag}").queue()
        sendSuccess(ctx.message)
    }
}

class DeHoistListener(private val variables: Variables) : ListenerAdapter() {

    private val regex = "[!\"#\$%&'()*+,-./](?:.*)".toRegex()
    private val dehoistChar = "▪"

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (shouldChangeName(event.member)) {
            // the char \uD82F\uDCA2 or \u1BCA2 is a null char that puts a member to the bottom
            event.guild.modifyNickname(event.member, dehoistChar + event.member.effectiveName)
                .reason("auto de-hoist").queue()
        }
    }

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        if (shouldChangeName(event.member)) {
            // the char \uD82F\uDCA2 or \u1BCA2 is a null char that puts a member to the bottom
            event.guild.modifyNickname(event.member, dehoistChar + event.member.effectiveName)
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
        return (
            !memberName.startsWith(dehoistChar) && matcher &&
                member.guild.selfMember.hasPermission(Permission.NICKNAME_MANAGE) &&
                GuildSettingsUtils.getGuild(member.guild.idLong, this.variables).isAutoDeHoist
            )
    }
}
