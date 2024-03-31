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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.sentry.Sentry
import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import me.duncte123.skybot.Variables
import me.duncte123.skybot.commands.guild.mod.ModBaseCommand
import me.duncte123.skybot.database.AbstractDatabase
import me.duncte123.skybot.entities.jda.DunctebotGuild
import me.duncte123.skybot.objects.command.CommandCategory
import me.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.util.concurrent.atomic.AtomicLong

class BlackListCommand : ModBaseCommand() {
    init {
        this.requiresArgs = true
        this.category = CommandCategory.ADMINISTRATION
        this.name = "blacklist"
        this.help = """Control the blacklisted words on your server
            |Note: **People that have the kick members permission will bypass the blacklist**
        """.trimMargin()
        this.usage = "<list/clear/import/add/remove> [word]"
        this.extraInfo = """```{prefix}$name list => Gives you a list of the current blacklisted words
        |{prefix}$name clear => Clears the blacklist
        |{prefix}$name import => Imports an exported blacklist
        |{prefix}$name add <word> => Adds a word to the blacklist
        |{prefix}$name remove <word> => Removes a word from the blacklist```
        """.trimMargin()
        this.userPermissions = arrayOf(Permission.MANAGE_SERVER)
        this.botPermissions = arrayOf(Permission.MESSAGE_MANAGE)
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args
        val sendMsg: (MessageCreateBuilder) -> Unit = {
            sendMsg(MessageConfig.Builder.fromCtx(ctx)
                .setMessageBuilder(it)
                .setFailureAction { thr ->
                    sendMsg(ctx, "Failed to send (attach file permission missing???): ${thr.message}")
                }
                .build())
        }

        when (args[0]) {
            "list", "export" -> {
                val guild = ctx.guild

                if (!guild.selfMember.hasPermission(ctx.channel.asGuildMessageChannel(), Permission.MESSAGE_ATTACH_FILES)) {
                    sendMsg(ctx, "This command requires me to be able to upload files to this channel")

                    return
                }

                listBlackList(
                    guild.settings.blacklistedWords,
                    guild,
                    ctx.author,
                    ctx.variables.jackson,
                    sendMsg
                )
                return
            }

            "clear" -> {
                if (!ctx.guild.selfMember.hasPermission(ctx.channel.asGuildMessageChannel(), Permission.MESSAGE_ATTACH_FILES)) {
                    sendMsg(ctx, "This command requires me to be able to upload files to this channel")

                    return
                }


                clearBlacklist(
                    ctx.database,
                    ctx.guild,
                    ctx.variables.jackson,
                    sendMsg
                )
                return
            }

            "import" -> {
                importBlackList(ctx)
                return
            }
        }

        if (args.size < 2) {
            sendMsg(ctx, "Missing arguments, check `${ctx.prefix}help $name`")

            return
        }

        when (args[0]) {
            "add" -> addWordToBlacklist(args[1].lowercase(), ctx.database, ctx.guild, ctx)
            "remove" -> removeWordFromBlacklist(args[1].lowercase(), ctx.database, ctx.guild, ctx)
            else -> sendMsg(ctx, "Unknown argument `${args[0]}` check `${ctx.prefix}help $name`")
        }
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addSubcommands(
            SubcommandData(
                "add",
                "Add a word to the blacklist"
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    "word",
                    "The word to add to the blacklist",
                    true
                )
            ),
            SubcommandData(
                "remove",
                "Remove a word from the blacklist"
            ).addOptions(
                OptionData(
                    OptionType.STRING,
                    "word",
                    "The word to add to the blacklist",
                    true
                )
            ),
            SubcommandData(
                "list",
                "Export the current blacklist to a list"
            ),
            SubcommandData(
                "import",
                "Import an exported blacklist"
            ).addOptions(
                OptionData(
                    OptionType.ATTACHMENT,
                    "file",
                    "The file created by running the list command.",
                    true
                )
            ),
            SubcommandData(
                "clear",
                "Clears the blacklist"
            ),
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, guild: DunctebotGuild, variables: Variables) {
        when (event.fullCommandName) {
            "blacklist list" -> {
                val blacklist = guild.settings.blacklistedWords

                listBlackList(
                    blacklist,
                    guild,
                    event.user,
                    variables.jackson
                ) {
                    event.reply(it.build()).queue()
                }
            }

            "blacklist clear" -> {
                clearBlacklist(
                    variables.database,
                    guild,
                    variables.jackson
                ) {
                    event.reply(it.build()).queue()
                }
            }

            else -> event.reply("NO! (also gg on breaking discord)").queue()
        }
    }

    private fun listBlackList(
        blacklist: List<String>,
        guild: Guild,
        author: User,
        jackson: ObjectMapper,
        sendMsg: (MessageCreateBuilder) -> Unit
    ) {
        if (blacklist.isEmpty()) {
            sendMsg(
                MessageCreateBuilder()
                    .setContent("The current blacklist is empty")
            )

            return
        }

        val listBytes = jackson.writeValueAsBytes(blacklist)
        val isOwner = author.idLong == guild.ownerIdLong

        sendMsg(
            MessageCreateBuilder()
                .setContent("Here is the current black list for ${if (isOwner) "your" else "this"} server")
                .addFiles(
                    FileUpload.fromData(
                        listBytes,
                        "blacklist_${guild.id}.json"
                    )
                )
        )
    }

    private fun clearBlacklist(
        database: AbstractDatabase,
        guild: DunctebotGuild,
        jackson: ObjectMapper,
        sendMsg: (MessageCreateBuilder) -> Unit
    ) {
        val blacklist = guild.settings.blacklistedWords

        if (blacklist.isEmpty()) {
            sendMsg(
                MessageCreateBuilder()
                    .setContent("The current blacklist is already empty")
            )
            return
        }

        database.clearBlacklist(guild.idLong)

        // backup blacklist before fully removing it.
        val listBytes = jackson.writeValueAsBytes(blacklist)

        guild.settings.blacklistedWords.clear()

        sendMsg(
            MessageCreateBuilder()
                .setContent("The blacklist has been cleared!\nYou can use the file attached to import your old blacklist in case this was a mistake.")
                .addFiles(
                    FileUpload.fromData(
                        listBytes,
                        "blacklist_${guild.id}.json"
                    )
                )
        )
    }

    private fun importBlackList(ctx: CommandContext) {
        val message = ctx.message
        val attachments = message.attachments

        if (attachments.isEmpty()) {
            sendMsg(ctx, "Please attach an exported blacklist file, you can get this with `${ctx.prefix}$name list`")

            return
        }

        val msgId = AtomicLong() // Bad ideas with duncte123 episode 5
        sendMsg(
            MessageConfig.Builder.fromCtx(ctx)
                .setMessage("Importing....")
                .setSuccessAction {
                    msgId.set(it.idLong)
                }
                .build()
        )

        val jackson = ctx.variables.jackson
        val current = ctx.guildSettings.blacklistedWords
        val guildId = ctx.guild.idLong
        attachments[0].proxy.download().thenAcceptAsync {
            try {
                val importedBlacklist = jackson.readValue(it, object : TypeReference<List<String>>() {})
                val filtered = importedBlacklist.filter { w -> !current.contains(w) }

                current.addAll(filtered)
                ctx.database.addWordsToBlacklist(guildId, filtered)

                ctx.edit(msgId, "Blacklist successfully imported")
            } catch (e: Exception) {
                Sentry.captureException(e)
                ctx.edit(msgId, "Error while importing blacklist: ${e.message}")
            } finally {
                it.close()
            }
        }
    }

    private fun addWordToBlacklist(
        word: String,
        database: AbstractDatabase,
        guild: DunctebotGuild,
        ctx: CommandContext,
    ) {
        val list = guild.settings.blacklistedWords

        if (list.contains(word)) {
            sendMsg(ctx, "This word is already in the blacklist")

            return
        }

        list.add(word)

        database.addWordToBlacklist(guild.idLong, word)

        sendMsg(ctx, "`$word` has been added to the blacklist")
    }

    private fun removeWordFromBlacklist(
        word: String,
        database: AbstractDatabase,
        guild: DunctebotGuild,
        ctx: CommandContext,
    ) {
        val list = guild.settings.blacklistedWords

        if (!list.contains(word)) {
            sendMsg(ctx, "This word is not in the blacklist")

            return
        }

        list.remove(word)

        database.removeWordFromBlacklist(guild.idLong, word)

        sendMsg(ctx, "`$word` has been removed from the blacklist")
    }

    private fun CommandContext.edit(id: AtomicLong, content: String) {
        val msg = id.get()

        if (msg > 0) {
            this.channel.editMessageById(msg, content).queue()
        } else {
            sendMsg(this, content)
        }
    }
}
