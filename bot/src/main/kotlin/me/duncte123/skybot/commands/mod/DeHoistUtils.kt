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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.botcommons.messaging.MessageUtils.sendSuccess
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.FinderUtils
import me.duncte123.skybot.utils.GuildSettingsUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.lang.Character.UnicodeBlock
import java.text.Normalizer
import kotlin.random.Random

// private val regex = "[!\"#\$%&'()*+,-./](?:.*)".toRegex()
// private const val dehoistChar = "▪"

// Allow special kinds of unicode.
private val allowedUnicodeBlocks = setOf(
    UnicodeBlock.ARABIC,
    UnicodeBlock.GREEK,
    UnicodeBlock.THAI,
)

private fun cleanUsername(username: String): String {
    return Normalizer.normalize(username, Normalizer.Form.NFKC)
        // TODO: is turning this into a char array more efficient?
        .toCharArray()
        .filter { it.isWhitespace() || it.isLetterOrDigit() || UnicodeBlock.of(it) in allowedUnicodeBlocks }
//        .map { if (it.isTitleCase()) it.lowercase() else it }
        .joinToString("") {
            if (it.isTitleCase()) it.lowercase() else it.toString()
        }
        .trim()
}

private val Member.cleanedDisplayName: String
    get() {
        val cleanedName = cleanUsername(effectiveName)

        if (cleanedName.isBlank()) {
            return "Member_${Random.nextInt(guild.memberCount)}"
        }

        if (cleanedName == effectiveName) {
            return effectiveName
        }

        return cleanedName
    }

private fun shouldDehoist(member: Member): Boolean {
    return member.cleanedDisplayName != member.effectiveName &&
        member.guild.selfMember.hasPermission(Permission.NICKNAME_MANAGE)
}

private fun canAutoDehoist(member: Member, variables: Variables): Boolean {
    return shouldDehoist(member) && GuildSettingsUtils.getGuild(member.guild.idLong, variables).isAutoDeHoist
}

class DeHoistCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.name = "dehoist"
        this.help = "De-hoists a user and cleans their username"
        this.usage = "<@user>/all"
        this.userPermissions = arrayOf(Permission.NICKNAME_MANAGE)
        this.botPermissions = arrayOf(Permission.NICKNAME_MANAGE)
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val selfMember = ctx.guild.selfMember
        val firstArg = args.first()

        if (firstArg == "all") {
            sendMsg(ctx, "Cleaning all members with a hoisted or unicode username, please wait...")

            ctx.guild.loadMembers {
                if (selfMember.canInteract(it) && shouldDehoist(it)) {
                    it.modifyNickname(it.cleanedDisplayName)
                        .reason("de-hoist/nickname cleaning by ${ctx.author.asTag}")
                        .queue()
                }
            }.onSuccess {
                sendMsg(ctx, "Cleaning complete!")
            }
            return
        }

        val foundMembers = FinderUtils.searchMembers(firstArg, ctx)

        if (foundMembers.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val toDehoist = foundMembers[0]
        val member = ctx.member

        if (!selfMember.canInteract(toDehoist)) {
            sendMsg(ctx, "I cannot alter that user")
            return
        }
        if (!member.canInteract(toDehoist)) {
            sendMsg(ctx, "You cannot change the nickname of that user")
            return
        }

        ctx.guild.modifyNickname(toDehoist, toDehoist.cleanedDisplayName)
            .reason("de-hoist/nickname cleaning ${ctx.author.asTag}").queue()
        sendSuccess(ctx.message)
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addOptions(
            OptionData(
                OptionType.USER,
                "user",
                "The user to dehoist, omit for checking everyone",
                false
            )
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val userOption = event.getOption("user")
        val selfMember = guild.selfMember

        if (userOption == null) {
            // DeHoist all
            event.deferReply().queue()
            guild.loadMembers {
                if (selfMember.canInteract(it) && shouldDehoist(it)) {
                    it.modifyNickname(it.cleanedDisplayName)
                        .reason("de-hoist/nickname cleaning by ${event.user.asTag}")
                        .queue()
                }
            }.onSuccess {
                event.hook.editOriginal("Cleaning complete!").queue()
            }
            return
        }

        val toDehoist = userOption.asMember

        if (toDehoist == null) {
            event.reply("Could not find user for your input")
                .setEphemeral(true)
                .queue()
            return
        }

        val moderator = event.member!!

        if (!selfMember.canInteract(toDehoist)) {
            event.reply("I cannot alter that user")
                .setEphemeral(true)
                .queue()
            return
        }

        if (!moderator.canInteract(toDehoist)) {
            event.reply("You cannot change the nickname of that user")
                .setEphemeral(true)
                .queue()
            return
        }

        guild.modifyNickname(toDehoist, toDehoist.cleanedDisplayName)
            .reason("de-hoist/nickname cleaning ${event.user.asTag}").queue()

        event.reply("Dehoisted ${event.user.asTag}").queue()
    }
}

class DeHoistListener(private val variables: Variables) : ListenerAdapter() {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (canAutoDehoist(event.member, variables)) {
            event.guild.modifyNickname(event.member, event.member.cleanedDisplayName)
                .reason("auto de-hoist").queue()
        }
    }

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        if (canAutoDehoist(event.member, variables)) {
            event.guild.modifyNickname(event.member, event.member.cleanedDisplayName)
                .reason("auto de-hoist").queue()
        }
    }
}
