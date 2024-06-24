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

import com.dunctebot.models.settings.WarnAction
import com.dunctebot.models.utils.DateUtils
import me.duncte123.botcommons.messaging.MessageUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.commands.guild.mod.TempBanCommand.getDuration
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.objects.command.Flag
import me.duncte123.skybot.utils.CommandUtils
import me.duncte123.skybot.utils.ModerationUtils.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException.ignore
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.ErrorResponse.CANNOT_SEND_TO_USER
import java.util.concurrent.TimeUnit

class WarnCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.name = "warn"
        this.help = "Warns a user\nWhen a user has reached 3 warnings they will be kicked from the server"
        this.usage = "<@user> [-r reason]"
        this.userPermissions = arrayOf(Permission.KICK_MEMBERS)
        this.botPermissions = arrayOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
        this.flags = arrayOf(
            Flag(
                'r',
                "reason",
                "Sets the reason for this warning"
            )
        )
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addOptions(
            OptionData(
                OptionType.USER,
                "user",
                "The user to warn",
                true
            ),
            OptionData(
                OptionType.STRING,
                "reason",
                "Why is this user getting warned?",
                false,
            )
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val target = event.getOption("user")!!.asMember

        if (target == null) {
            event.reply("No user found for your input?")
                .setEphemeral(true)
                .queue()
            return
        }

        val selfUser = event.jda.selfUser

        if (target.idLong == selfUser.idLong) {
            event.reply("What did I do wrong? \uD83D\uDE22").queue()
            return
        }


        val moderator = event.member!!
        val channel = event.channel

        // Check if we can interact
        if (!canInteract(moderator, target, "warn", channel)) {
            return
        }

        val modUser = moderator.user
        val reason = event.getOption("reason")?.asString ?: ""
        val targetUser = target.user
        val dmMessage = """You have been warned by ${modUser.asTag}
            |Reason: ${if (reason.isEmpty()) "No reason given" else "`$reason`"}
        """.trimMargin()

        event.deferReply().queue();

        // add the new warning to the database
        val future = variables.database.createWarning(
            modUser.idLong,
            target.idLong,
            guild.idLong,
            reason
        )

        modLog(modUser, targetUser, "warned", reason, null, guild)

        // Yes we can warn bots (cuz why not) but we cannot dm them
        if (!targetUser.isBot) {
            targetUser.openPrivateChannel()
                .flatMap { it.sendMessage(dmMessage) }
                .queue(null, ignore(CANNOT_SEND_TO_USER))
        }

        event.hook.editOriginal("Warned ${target.user.asTag}").queue()

        // Wait for the request to pass and then get the updated warn count
        future.get()

        val warnCount = variables.database.getWarningCountForUser(targetUser.idLong, guild.idLong).get()
        val action = getSelectedWarnAction(warnCount, guild)

        if (action != null) {
            invokeAction(warnCount, action, modUser, target, guild, variables)
        }
    }

    override fun execute(ctx: CommandContext) {
        sendMsg(ctx, "This is a slash command now :)")
    }

    private fun getSelectedWarnAction(threshold: Int, guild: DunctebotGuild): WarnAction? {
        if (!CommandUtils.isGuildPatron(guild)) {
            val action = guild.settings.warnActions.firstOrNull()

            return if (action != null && threshold >= action.threshold) {
                action
            } else {
                null
            }
        }

        return guild.settings
            .warnActions
            // we reverse the list so we start with the highest one
            // preventing that everything is selected for the lowest number
            .reversed()
            .find { threshold >= it.threshold }
    }

    private fun invokeAction(warnings: Int, action: WarnAction, modUser: User, target: Member, guild: DunctebotGuild, variables: Variables) {
        val targetUser = target.user

        if ((action.type == WarnAction.Type.MUTE || action.type == WarnAction.Type.TEMP_MUTE) &&
            !muteRoleCheck(guild)
        ) {
            modLog(
                "[warn actions] Failed to apply automatic mute `${
                    targetUser.asTag
                }` as there is no mute role set in the settings of this server",
                guild
            )
            return
        }

        // That's a lot of duped mod logs
        when (action.type) {
            WarnAction.Type.MUTE -> {
                applyMuteRole(target, guild)
                modLog(modUser, targetUser, "muted", "Reached $warnings warnings", null, guild)
            }
            WarnAction.Type.TEMP_MUTE -> {
                applyMuteRole(target, guild)

                val (finalDate, dur) = "${action.duration}m".toDuration()

                variables.database.createMute(
                    modUser.idLong,
                    targetUser.idLong,
                    targetUser.asTag,
                    finalDate,
                    guild.idLong
                )

                modLog(modUser, targetUser, "muted", "Reached $warnings warnings", dur, guild)
            }
            WarnAction.Type.KICK -> {
                guild.kick(target).reason("Reached $warnings warnings").queue()
                modLog(modUser, targetUser, "kicked", "Reached $warnings warnings", null, guild)
            }
            WarnAction.Type.TEMP_BAN -> {
                val (finalUnbanDate, dur) = "${action.duration}d".toDuration()

                variables.database.createBan(
                    modUser.idLong,
                    targetUser.idLong,
                    finalUnbanDate,
                    guild.idLong
                )

                guild.ban(target, 0, TimeUnit.DAYS)
                    .reason("Reached $warnings warnings").queue()
                modLog(modUser, targetUser, "banned", "Reached $warnings warnings", dur, guild)
            }
            WarnAction.Type.BAN -> {
                guild.ban(target, 0, TimeUnit.DAYS)
                    .reason("Reached $warnings warnings").queue()
                modLog(modUser, targetUser, "banned", "Reached $warnings warnings", null, guild)
            }
        }
    }

    private fun applyMuteRole(target: Member, guild: DunctebotGuild) {
        val roleId = guild.settings.muteRoleId
        val role = guild.getRoleById(roleId)

        if (role == null) {
            modLog(
                "[warn actions] Failed to apply automatic mute `${
                    target.user.asTag
                }` as I could not find the role that is specified in the settings",
                guild
            )
            return
        }

        guild.addRoleToMember(target, role).queue()
    }

    private fun String.toDuration(): Pair<String, String> {
        val duration = getDuration(this, null, null, null)!!

        return DateUtils.getDatabaseDateFormat(duration) to duration.toString()
    }

    private fun muteRoleCheck(guild: DunctebotGuild) = guild.settings.muteRoleId > 0
}
