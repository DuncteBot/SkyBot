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

package ml.duncte123.skybot.commands.admin

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.sentry.Sentry
import me.duncte123.botcommons.messaging.MessageConfig
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.database.AbstractDatabase
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
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

        when (args[0]) {
            "list", "export" -> {
                listBlackList(ctx.guild.settings.blacklistedWords, ctx, ctx.variables.jackson)
                return
            }
            "clear" -> {
                clearBlacklist(ctx.database, ctx.guild, ctx)
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

    private fun listBlackList(blacklist: List<String>, ctx: CommandContext, jackson: ObjectMapper) {
        if (blacklist.isEmpty()) {
            sendMsg(ctx, "The current blacklist is empty")

            return
        }

        if (!ctx.guild.selfMember.hasPermission(ctx.channel, Permission.MESSAGE_ATTACH_FILES)) {
            sendMsg(ctx, "This command requires me to be able to upload files to this channel")

            return
        }

        val listBytes = jackson.writeValueAsBytes(blacklist)
        val isOwner = ctx.author.idLong == ctx.guild.ownerIdLong

        ctx.channel.sendMessage("Here is the current black list for ${if (isOwner) "your" else "this"} server")
            .addFile(
                listBytes,
                "blacklist_${ctx.guild.id}.json"
            )
            .queue(null) {
                sendMsg(ctx, "This command requires me to be able to upload files to this channel")
            }
    }

    private fun clearBlacklist(database: AbstractDatabase, guild: DunctebotGuild, ctx: CommandContext) {
        database.clearBlacklist(guild.idLong)

        guild.settings.blacklistedWords.clear()

        sendMsg(ctx, "The blacklist has been cleared")
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
        attachments[0].retrieveInputStream().thenAcceptAsync {
            try {
                val importedBlacklist = jackson.readValue<List<String>>(it, object : TypeReference<List<String>>() {})
                val filtered = importedBlacklist.filter { w -> !current.contains(w) }

                current.addAll(filtered)
                ctx.database.addWordsToBlacklist(guildId, filtered)

                ctx.channel.edit(msgId, "Blacklist successfully imported")
            } catch (e: Exception) {
                Sentry.captureException(e)
                ctx.channel.edit(msgId, "Error while importing blacklist: ${e.message}")
            } finally {
                it.close()
            }
        }
    }

    private fun addWordToBlacklist(word: String, database: AbstractDatabase, guild: DunctebotGuild, ctx: CommandContext) {
        val list = guild.settings.blacklistedWords

        if (list.contains(word)) {
            sendMsg(ctx, "This word is already in the blacklist")

            return
        }

        list.add(word)

        database.addWordToBlacklist(guild.idLong, word)

        sendMsg(ctx, "`$word` has been added to the blacklist")
    }

    private fun removeWordFromBlacklist(word: String, database: AbstractDatabase, guild: DunctebotGuild, ctx: CommandContext) {
        val list = guild.settings.blacklistedWords

        if (!list.contains(word)) {
            sendMsg(ctx, "This word is not in the blacklist")

            return
        }

        list.remove(word)

        database.removeWordFromBlacklist(guild.idLong, word)

        sendMsg(ctx, "`$word` has been removed from the blacklist")
    }

    private fun TextChannel.edit(id: AtomicLong, content: String) {
        val msg = id.get()

        if (msg > 0) {
            this.editMessageById(msg, content).queue()
        } else {
            sendMsg(this, content)
        }
    }
}
