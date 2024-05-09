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

package me.duncte123.skybot.commands.admin

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import gnu.trove.map.TLongLongMap
import gnu.trove.map.hash.TLongLongHashMap
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext
import me.duncte123.skybot.utils.FinderUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import java.util.stream.Collectors

const val RES_OK = 0
const val RES_NO_CHAN = -1
const val RES_NO_ROLE = -2
const val RES_ROLE_NOT_INTERACT = -3

class VcAutoRoleCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.category = CommandCategory.ADMINISTRATION
        this.name = "vcautorole"
        this.help = "Gives a role to a user when they join a specified voice channel"
        this.usage = "<add/remove/off/list> [voice channel] [@role]"
        this.extraInfo = """`{prefix}$name add <voice channel> <@role>`
        |`{prefix}$name remove <voice channel>`
        |`{prefix}$name off`
        |`{prefix}$name list`
        """.trimMargin()
        this.userPermissions = arrayOf(Permission.MANAGE_SERVER)
        this.botPermissions = arrayOf(Permission.MANAGE_SERVER, Permission.MANAGE_ROLES)
    }

    override fun execute(ctx: CommandContext) {
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache

        if (args.size == 1) {
            if (!vcAutoRoleCache.containsKey(guild.idLong)) {
                sendMsg(ctx, "No vc autorole has been set for this server")
                return
            }

            if (args[0] == "list") {
                val embed = listAutoVcRoles(guild, ctx.variables)

                sendEmbed(ctx, embed)
                return
            }

            if (args[0] != "off") {
                sendMsg(ctx, "Missing arguments, check `${ctx.prefix}help $name`")
                return
            }

            vcAutoRoleCache.remove(guild.idLong)
            ctx.database.removeVcAutoRoleForGuild(guild.idLong)

            sendMsg(ctx, "All Auto VC Roles has been disabled")
            return
        }

        if (args.size > 1) {
            if (args[0] == "add") {
                addVcAutoRole(ctx)
                return
            }

            if (args[0] == "remove") {
                removeVcAutoRole(ctx)
                return
            }
        }

        sendMsg(ctx, "Unknown operation, check `${ctx.prefix}$name`")
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData
            .addSubcommands(
                SubcommandData(
                    "add",
                    "Link a role and a voice channel together"
                ).addOptions(
                    OptionData(
                        OptionType.ROLE,
                        "role",
                        "The role to unlink from the voice channel",
                        true
                    ),
                    OptionData(
                        OptionType.CHANNEL,
                        "voice_channel",
                        "The voice channel to unlink all roles from",
                        true
                    ),
                ),
                SubcommandData(
                    "add-all",
                    "Link a role to ALL voice channels"
                ).addOptions(
                    OptionData(
                        OptionType.ROLE,
                        "role",
                        "The role to unlink from the voice channel",
                        true
                    ),
                ),
                SubcommandData(
                    "remove",
                    "Unlink a voice channel from a role"
                ).addOptions(
                    OptionData(
                        OptionType.CHANNEL,
                        "voice_channel",
                        "The voice channel to unlink all roles from",
                        true
                    ),
                    // TODO: option not valid yet
                    /*OptionData(
                        OptionType.ROLE,
                        "role",
                        "The role to unlink from the voice channel",
                        false
                    ),*/
                ),
                SubcommandData(
                    "off",
                    "Unlink **ALL** voice channels from **ALL** roles"
                ),
                SubcommandData(
                    "list",
                    "List your current configuration"
                ),
            )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        when (event.subcommandName) {
            "list" -> {
                val embed = listAutoVcRoles(guild, variables)

                event.replyEmbeds(embed.build()).queue()
            }

            "add" -> addSingleVcAutoRoleFromSlashEvent(event, guild, variables)

            "add-all" -> addVcAutoRoleToAllChannels(event, guild, variables)

            "remove" -> removeSingleVcAutoRoleFromSlashEvent(event, guild, variables)

            "off" -> {
                variables.vcAutoRoleCache.remove(guild.idLong)
                variables.database.removeVcAutoRoleForGuild(guild.idLong)

                event.reply("All vc autoroles removed for this server!").queue()
            }

            else -> event.reply("mi ne kompreni").queue()
        }
    }

    private fun removeVcAutoRole(ctx: CommandContext) {
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache
        val cache = vcAutoRoleCache.get(guild.idLong) ?: vcAutoRoleCache.put(guild.idLong, TLongLongHashMap())

        val foundVoiceChannels = FinderUtils.searchAudioChannels(args[1], ctx)

        if (foundVoiceChannels.isEmpty()) {
            sendMsg(
                ctx,
                String.format(
                    "I could not find any voice channels for `%s`%n" +
                        "TIP: If your voice channel name has spaces \"Surround it with quotes\" to give it as one argument",
                    args[1]
                )
            )

            return
        }

        val targetChannel = foundVoiceChannels[0].idLong

        if (!cache.containsKey(targetChannel)) {
            sendMsg(ctx, "This voice channel does not have an autorole set")
            return
        }

        cache.remove(targetChannel)
        ctx.database.removeVcAutoRole(targetChannel)
        sendMsg(ctx, "Autorole removed for <#$targetChannel>")
    }

    private fun addVcAutoRole(ctx: CommandContext) {
        val guild = ctx.guild
        val args = ctx.args
        val vcAutoRoleCache = ctx.variables.vcAutoRoleCache

        val foundRoles = FinderUtil.findRoles(args[2], guild)

        if (foundRoles.isEmpty()) {
            sendMsg(
                ctx,
                String.format(
                    "I could not find any role for `%s`%n" +
                        "TIP: If your role name has spaces \"Surround it with quotes\" to give it as one argument",
                    args[2]
                )
            )

            return
        }

        val targetRole = foundRoles[0].idLong
        var cache: TLongLongMap? = vcAutoRoleCache.get(guild.idLong)

        if (cache == null) {
            val new = TLongLongHashMap()

            vcAutoRoleCache.put(guild.idLong, new)
            cache = new
        }

        if (args[1].lowercase() == "all") {
            val ids = guild.voiceChannelCache.applyStream {
                it.map(VoiceChannel::getIdLong).collect(Collectors.toList())
            }!!

            ctx.database.setVcAutoRoleBatch(guild.idLong, ids, targetRole)
            ids.forEach { cache.put(it, targetRole) }

            sendMsg(
                ctx,
                "Role <@&$targetRole> will now be applied to a user when they join any voice channel " +
                    "(excluding ones that are created after the command was ran)"
            )
        } else {
            val foundVoiceChannels = FinderUtils.searchAudioChannels(args[1], ctx)

            if (foundVoiceChannels.isEmpty()) {
                sendMsg(
                    ctx,
                    String.format(
                        "I could not find any voice channels for `%s`%n" +
                            "TIP: If your voice channel name has spaces \"Surround it with quotes\" to give it as one argument",
                        args[1]
                    )
                )

                return
            }

            val targetChannel = foundVoiceChannels[0].idLong

            cache.put(targetChannel, targetRole)
            ctx.database.setVcAutoRole(guild.idLong, targetChannel, targetRole)

            sendMsg(ctx, "Role <@&$targetRole> will now be applied to a user when they join <#$targetChannel>")
        }
    }

    private fun addSingleVcAutoRoleFromSlashEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val channel = event.getOption("voice_channel")!!.asChannel

        if (channel.type != ChannelType.VOICE && channel.type != ChannelType.STAGE) {
            event.reply("The specified channel is not valid, you can only specify stage or voice channels.").queue()
            return
        }

        val role = event.getOption("role")!!.asRole

        if (!guild.selfMember.canInteract(role)) {
            event.reply("I cannot interact with that role, make sure that it is below my highest role!").queue()
            return
        }

        val audioChannel = channel.asAudioChannel()

        val result = addSingleVcAutoRole(
            audioChannel,
            role,
            guild,
            variables
        )

        when (result) {
            RES_OK -> {
                event.reply(
                    "Role <@&${role.id}> will now be applied to a user when they join <#${audioChannel.id}>"
                ).queue()
            }

            RES_ROLE_NOT_INTERACT -> {
                event.reply("I cannot interact with that role, make sure that it is below my highest role!").queue()
            }

            else -> event.reply("Something went wrong!").queue()
        }
    }

    private fun addVcAutoRoleToAllChannels(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val role = event.getOption("role")!!.asRole

        if (!guild.selfMember.canInteract(role)) {
            event.reply("I cannot interact with that role, make sure that it is below my highest role!").queue()
            return
        }


        val vcAutoRoleCache = variables.vcAutoRoleCache
        val cache: TLongLongMap = vcAutoRoleCache.putIfAbsent(guild.idLong, TLongLongHashMap())
        val targetRole = role.idLong

        // TODO: include other audio channels as well
        val ids = guild.voiceChannelCache.applyStream {
            it.map(VoiceChannel::getIdLong).toList()
        }!!

        variables.database.setVcAutoRoleBatch(guild.idLong, ids, targetRole)
        ids.forEach { cache.put(it, targetRole) }

        event.reply(
            "Role <@&$targetRole> will now be applied to a user when they join any voice channel " +
                "(excluding ones that are created after the command was ran)"
        ).queue()
    }

    private fun removeSingleVcAutoRoleFromSlashEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        val channel = event.getOption("voice_channel")!!.asChannel

        if (channel.type != ChannelType.VOICE && channel.type != ChannelType.STAGE) {
            event.reply("The specified channel is not valid, you can only specify stage or voice channels.").queue()
            return
        }

        val audioChannel = channel.asAudioChannel()

        val result = removeSingleVcAutoRole(
            audioChannel,
            guild,
            variables
        )

        when (result) {
            RES_OK -> {
                event.reply(
                    "All autoroles removed from <#${audioChannel.id}>"
                ).queue()
            }

            RES_NO_CHAN -> {
                event.reply("That channel did not have any autoroles set").queue()
            }

            else -> event.reply("Something went wrong!").queue()
        }
    }

    private fun addSingleVcAutoRole(
        channel: AudioChannel,
        role: Role,
        guild: Guild,
        variables: Variables
    ): Int {
        if (!guild.selfMember.canInteract(role)) {
            return RES_ROLE_NOT_INTERACT
        }

        val vcAutoRoleCache = variables.vcAutoRoleCache
        val cache: TLongLongMap = vcAutoRoleCache.putIfAbsent(guild.idLong, TLongLongHashMap())
        val targetChannel = channel.idLong
        val targetRole = role.idLong

        cache.put(targetChannel, targetRole)
        variables.database.setVcAutoRole(guild.idLong, targetChannel, targetRole)

        return RES_OK
    }

    private fun removeSingleVcAutoRole(
        channel: AudioChannel,
        guild: Guild,
        variables: Variables
    ): Int {
        val vcAutoRoleCache = variables.vcAutoRoleCache
        val cache: TLongLongMap = vcAutoRoleCache.putIfAbsent(guild.idLong, TLongLongHashMap())
        val targetChannel = channel.idLong

        if (!cache.containsKey(targetChannel)) {
            return RES_NO_CHAN
        }

        cache.remove(targetChannel)
        variables.database.removeVcAutoRole(targetChannel)

        return RES_OK
    }

    private fun listAutoVcRoles(
        guild: Guild,
        variables: Variables
    ): EmbedBuilder {
        val cache = variables.vcAutoRoleCache
        val guildId = guild.idLong

        if (!cache.containsKey(guildId)) {
            return  EmbedUtils.getDefaultEmbed(guildId)
                .setDescription("No vc autorole has been set for this server")
        }

        val items = cache.get(guildId)
        val embed = EmbedUtils.getDefaultEmbed(guildId)
            .setDescription("List of vc auto roles:\n")

        items.forEachEntry { vc, role ->

            embed.appendDescription("<#$vc> => <@&$role>\n")

            return@forEachEntry true
        }


        return embed
    }
}
